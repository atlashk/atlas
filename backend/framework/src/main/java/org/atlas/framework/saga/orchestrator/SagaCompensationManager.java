package org.atlas.framework.saga.orchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.entity.*;
import org.atlas.framework.saga.event.SagaEventPublisher;
import org.atlas.framework.saga.repository.SagaCompensationRepository;
import org.atlas.framework.saga.repository.SagaStepRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Enhanced Saga Compensation Manager that handles compensation logic with improved
 * error handling, retry mechanisms, and performance optimizations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCompensationManager {

  private final SagaCompensationRepository compensationRepository;
  private final SagaStepRepository stepRepository;
  private final SagaOrchestratorRegistry orchestratorRegistry;
  private final SagaEventPublisher eventPublisher;
  private final Executor sagaExecutor;

  // Performance tracking
  private final Map<String, CompensationStatistics> compensationStats = new ConcurrentHashMap<>();
  private final AtomicInteger activeCompensations = new AtomicInteger(0);

  /**
   * Start compensation for a saga asynchronously
   */
  public CompletableFuture<Void> startCompensationAsync(@NotBlank String sagaId) {
    return CompletableFuture.runAsync(() -> {
      try {
        startCompensation(sagaId);
      } catch (Exception e) {
        log.error("Async compensation failed for saga: {}", sagaId, e);
        throw new RuntimeException("Compensation failed", e);
      }
    }, sagaExecutor);
  }

  /**
   * Start compensation for a saga
   */
  @Transactional
  public void startCompensation(@NotBlank String sagaId) {
    long startTime = System.currentTimeMillis();
    activeCompensations.incrementAndGet();
    
    try {
      log.info("Starting compensation for saga: {}", sagaId);
      
      // Get completed steps in reverse order for compensation
      List<SagaStepEntity> completedSteps = getCompletedStepsInReverseOrder(sagaId);
      
      if (completedSteps.isEmpty()) {
        log.info("No completed steps found for compensation in saga: {}", sagaId);
        return;
      }
      
      // Create compensation entities for each completed step
      List<SagaCompensationEntity> compensations = createCompensationEntities(completedSteps);
      
      // Save all compensations
      compensationRepository.saveAll(compensations);
      
      // Publish compensation started event
      eventPublisher.publishCompensationStarted(sagaId, compensations.size());
      
      // Execute compensations
      executeCompensations(sagaId, compensations);
      
      // Update statistics
      updateCompensationStatistics(sagaId, startTime, compensations.size(), true);
      
      log.info("Compensation started successfully for saga: {} with {} steps", 
          sagaId, compensations.size());
          
    } catch (Exception e) {
      updateCompensationStatistics(sagaId, startTime, 0, false);
      log.error("Failed to start compensation for saga: {}", sagaId, e);
      throw new SagaCompensationException("Failed to start compensation for saga: " + sagaId, e);
    } finally {
      activeCompensations.decrementAndGet();
    }
  }

  /**
   * Execute pending compensations for a saga
   */
  @Transactional
  public void executePendingCompensations(@NotBlank String sagaId) {
    try {
      List<SagaCompensationEntity> pendingCompensations = compensationRepository
          .findBySagaIdAndStatus(sagaId, SagaCompensationStatus.PENDING);
      
      if (pendingCompensations.isEmpty()) {
        log.debug("No pending compensations found for saga: {}", sagaId);
        return;
      }
      
      log.info("Executing {} pending compensations for saga: {}", 
          pendingCompensations.size(), sagaId);
      
      executeCompensations(sagaId, pendingCompensations);
      
    } catch (Exception e) {
      log.error("Failed to execute pending compensations for saga: {}", sagaId, e);
      throw new SagaCompensationException("Failed to execute pending compensations", e);
    }
  }

  /**
   * Execute a list of compensations
   */
  private void executeCompensations(@NotBlank String sagaId, 
                                   @NotNull List<SagaCompensationEntity> compensations) {
    
    // Sort compensations by step order (reverse for compensation)
    compensations.sort((c1, c2) -> Integer.compare(c2.getStepOrder(), c1.getStepOrder()));
    
    for (SagaCompensationEntity compensation : compensations) {
      try {
        executeCompensation(compensation);
      } catch (Exception e) {
        log.error("Failed to execute compensation for step: {} in saga: {}", 
            compensation.getStepName(), sagaId, e);
        
        // Continue with other compensations even if one fails
        handleCompensationFailure(compensation, e.getMessage());
      }
    }
  }

  /**
   * Execute a single compensation
   */
  @Transactional
  public void executeCompensation(@NotNull SagaCompensationEntity compensation) {
    String sagaId = compensation.getSagaId();
    String stepName = compensation.getStepName();
    
    try {
      log.debug("Executing compensation for step: {} in saga: {}", stepName, sagaId);
      
      // Mark compensation as in progress
      compensation.start();
      compensationRepository.save(compensation);
      
      // Publish compensation step started event
      eventPublisher.publishCompensationStepStarted(sagaId, stepName);
      
      // Get orchestrator and compensation method
      String orchestratorName = compensation.getOrchestratorName();
      Object orchestrator = orchestratorRegistry.getOrchestrator(orchestratorName);
      Method compensationMethod = orchestratorRegistry.getCompensationMethod(orchestratorName, stepName);
      
      if (compensationMethod == null) {
        log.warn("No compensation method found for step: {} in orchestrator: {}", 
            stepName, orchestratorName);
        compensation.markCompleted();
        compensationRepository.save(compensation);
        return;
      }
      
      // Execute compensation method
      executeCompensationMethod(orchestrator, compensationMethod, compensation);
      
      // Mark compensation as completed
      compensation.markCompleted();
      compensationRepository.save(compensation);
      
      // Publish compensation step completed event
      eventPublisher.publishCompensationStepCompleted(sagaId, stepName);
      
      log.debug("Compensation completed successfully for step: {} in saga: {}", stepName, sagaId);
      
    } catch (Exception e) {
      log.error("Compensation execution failed for step: {} in saga: {}", stepName, sagaId, e);
      handleCompensationFailure(compensation, e.getMessage());
      throw new SagaCompensationException(
          String.format("Compensation failed for step: %s in saga: %s", stepName, sagaId), e);
    }
  }

  /**
   * Execute compensation method using reflection
   */
  private void executeCompensationMethod(@NotNull Object orchestrator, 
                                       @NotNull Method compensationMethod,
                                       @NotNull SagaCompensationEntity compensation) {
    try {
      // Make method accessible if needed
      if (!compensationMethod.isAccessible()) {
        compensationMethod.setAccessible(true);
      }
      
      // Prepare method parameters (context, compensation data, etc.)
      Object[] parameters = prepareCompensationParameters(compensationMethod, compensation);
      
      // Invoke compensation method
      Object result = compensationMethod.invoke(orchestrator, parameters);
      
      // Handle async results if needed
      if (result instanceof CompletableFuture) {
        CompletableFuture<?> future = (CompletableFuture<?>) result;
        future.get(); // Wait for completion
      }
      
    } catch (Exception e) {
      throw new SagaCompensationException("Failed to execute compensation method", e);
    }
  }

  /**
   * Prepare parameters for compensation method invocation
   */
  private Object[] prepareCompensationParameters(@NotNull Method compensationMethod,
                                               @NotNull SagaCompensationEntity compensation) {
    Class<?>[] parameterTypes = compensationMethod.getParameterTypes();
    Object[] parameters = new Object[parameterTypes.length];
    
    for (int i = 0; i < parameterTypes.length; i++) {
      Class<?> paramType = parameterTypes[i];
      
      if (paramType.equals(SagaCompensationEntity.class)) {
        parameters[i] = compensation;
      } else if (paramType.equals(String.class)) {
        // Assume it's saga ID or step name
        parameters[i] = compensation.getSagaId();
      } else if (paramType.equals(Map.class)) {
        // Provide compensation data
        parameters[i] = compensation.getCompensationData();
      } else {
        // Default to null for unknown types
        parameters[i] = null;
      }
    }
    
    return parameters;
  }

  /**
   * Handle compensation failure
   */
  @Transactional
  public void handleCompensationFailure(@NotNull SagaCompensationEntity compensation, 
                                       @NotBlank String errorMessage) {
    String sagaId = compensation.getSagaId();
    String stepName = compensation.getStepName();
    
    try {
      log.warn("Handling compensation failure for step: {} in saga: {}", stepName, sagaId);
      
      if (compensation.canRetry()) {
        // Schedule retry
        compensation.startRetry();
        compensation.setErrorMessage(errorMessage);
        compensationRepository.save(compensation);
        
        log.info("Scheduled retry for compensation step: {} in saga: {} (attempt: {})", 
            stepName, sagaId, compensation.getRetryCount());
        
        // Publish compensation retry event
        eventPublisher.publishCompensationStepRetry(sagaId, stepName, compensation.getRetryCount());
        
      } else {
        // Mark as failed
        compensation.markFailed();
        compensation.setErrorMessage(errorMessage);
        compensationRepository.save(compensation);
        
        log.error("Compensation failed permanently for step: {} in saga: {}", stepName, sagaId);
        
        // Publish compensation failed event
        eventPublisher.publishCompensationStepFailed(sagaId, stepName, errorMessage);
      }
      
    } catch (Exception e) {
      log.error("Failed to handle compensation failure for step: {} in saga: {}", 
          stepName, sagaId, e);
    }
  }

  /**
   * Retry failed compensations
   */
  @Transactional
  public void retryFailedCompensations(@NotBlank String sagaId) {
    try {
      List<SagaCompensationEntity> failedCompensations = compensationRepository
          .findBySagaIdAndStatus(sagaId, SagaCompensationStatus.FAILED);
      
      List<SagaCompensationEntity> retryableCompensations = failedCompensations.stream()
          .filter(SagaCompensationEntity::canRetry)
          .filter(SagaCompensationEntity::isRetryTimeReached)
          .collect(Collectors.toList());
      
      if (retryableCompensations.isEmpty()) {
        log.debug("No retryable compensations found for saga: {}", sagaId);
        return;
      }
      
      log.info("Retrying {} failed compensations for saga: {}", 
          retryableCompensations.size(), sagaId);
      
      for (SagaCompensationEntity compensation : retryableCompensations) {
        try {
          executeCompensation(compensation);
        } catch (Exception e) {
          log.error("Retry failed for compensation step: {} in saga: {}", 
              compensation.getStepName(), sagaId, e);
        }
      }
      
    } catch (Exception e) {
      log.error("Failed to retry compensations for saga: {}", sagaId, e);
      throw new SagaCompensationException("Failed to retry compensations", e);
    }
  }

  /**
   * Check compensation status for a saga
   */
  public CompensationStatus getCompensationStatus(@NotBlank String sagaId) {
    try {
      List<SagaCompensationEntity> compensations = compensationRepository.findBySagaId(sagaId);
      
      if (compensations.isEmpty()) {
        return CompensationStatus.NOT_STARTED;
      }
      
      long totalCount = compensations.size();
      long completedCount = compensations.stream()
          .filter(c -> c.getStatus() == SagaCompensationStatus.COMPLETED)
          .count();
      long failedCount = compensations.stream()
          .filter(c -> c.getStatus() == SagaCompensationStatus.FAILED)
          .count();
      long inProgressCount = compensations.stream()
          .filter(c -> c.getStatus() == SagaCompensationStatus.IN_PROGRESS)
          .count();
      
      if (completedCount == totalCount) {
        return CompensationStatus.COMPLETED;
      } else if (failedCount > 0 && inProgressCount == 0) {
        return CompensationStatus.FAILED;
      } else if (inProgressCount > 0) {
        return CompensationStatus.IN_PROGRESS;
      } else {
        return CompensationStatus.PENDING;
      }
      
    } catch (Exception e) {
      log.error("Failed to get compensation status for saga: {}", sagaId, e);
      return CompensationStatus.UNKNOWN;
    }
  }

  /**
   * Get compensation progress for a saga
   */
  public CompensationProgress getCompensationProgress(@NotBlank String sagaId) {
    try {
      List<SagaCompensationEntity> compensations = compensationRepository.findBySagaId(sagaId);
      
      if (compensations.isEmpty()) {
        return CompensationProgress.builder()
            .sagaId(sagaId)
            .totalSteps(0)
            .completedSteps(0)
            .failedSteps(0)
            .pendingSteps(0)
            .progressPercentage(0.0)
            .status(CompensationStatus.NOT_STARTED)
            .build();
      }
      
      int totalSteps = compensations.size();
      int completedSteps = (int) compensations.stream()
          .filter(c -> c.getStatus() == SagaCompensationStatus.COMPLETED)
          .count();
      int failedSteps = (int) compensations.stream()
          .filter(c -> c.getStatus() == SagaCompensationStatus.FAILED)
          .count();
      int pendingSteps = totalSteps - completedSteps - failedSteps;
      
      double progressPercentage = totalSteps > 0 ? 
          (double) completedSteps / totalSteps * 100.0 : 0.0;
      
      return CompensationProgress.builder()
          .sagaId(sagaId)
          .totalSteps(totalSteps)
          .completedSteps(completedSteps)
          .failedSteps(failedSteps)
          .pendingSteps(pendingSteps)
          .progressPercentage(progressPercentage)
          .status(getCompensationStatus(sagaId))
          .compensations(new ArrayList<>(compensations))
          .build();
      
    } catch (Exception e) {
      log.error("Failed to get compensation progress for saga: {}", sagaId, e);
      throw new SagaCompensationException("Failed to get compensation progress", e);
    }
  }

  /**
   * Get completed steps in reverse order for compensation
   */
  private List<SagaStepEntity> getCompletedStepsInReverseOrder(@NotBlank String sagaId) {
    List<SagaStepEntity> completedSteps = stepRepository
        .findBySagaIdAndStatus(sagaId, SagaStepStatus.COMPLETED);
    
    // Sort by step order in reverse (highest order first for compensation)
    completedSteps.sort((s1, s2) -> Integer.compare(s2.getStepOrder(), s1.getStepOrder()));
    
    return completedSteps;
  }

  /**
   * Create compensation entities for completed steps
   */
  private List<SagaCompensationEntity> createCompensationEntities(
      @NotNull List<SagaStepEntity> completedSteps) {
    
    List<SagaCompensationEntity> compensations = new ArrayList<>();
    
    for (SagaStepEntity step : completedSteps) {
      // Only create compensation if step has compensation logic
      if (step.hasCompensation()) {
        SagaCompensationEntity compensation = SagaCompensationEntity.builder()
            .sagaId(step.getSagaId())
            .stepId(step.getId())
            .stepName(step.getStepName())
            .stepOrder(step.getStepOrder())
            .orchestratorName(step.getOrchestratorName())
            .status(SagaCompensationStatus.PENDING)
            .compensationData(step.getStepData())
            .maxRetries(step.getMaxRetries())
            .retryDelayMs(step.getRetryDelayMs())
            .createdAt(LocalDateTime.now())
            .build();
        
        compensations.add(compensation);
      }
    }
    
    return compensations;
  }

  /**
   * Update compensation statistics
   */
  private void updateCompensationStatistics(@NotBlank String sagaId, long startTime, 
                                          int stepCount, boolean success) {
    long duration = System.currentTimeMillis() - startTime;
    
    CompensationStatistics stats = compensationStats.computeIfAbsent(sagaId, 
        k -> new CompensationStatistics());
    
    stats.incrementAttempts();
    stats.addDuration(duration);
    stats.setStepCount(stepCount);
    
    if (success) {
      stats.incrementSuccesses();
    } else {
      stats.incrementFailures();
    }
  }

  /**
   * Get compensation statistics
   */
  public Map<String, CompensationStatistics> getCompensationStatistics() {
    return new HashMap<>(compensationStats);
  }

  /**
   * Get active compensations count
   */
  public int getActiveCompensationsCount() {
    return activeCompensations.get();
  }

  // Inner classes for data transfer

  /**
   * Compensation status enumeration
   */
  public enum CompensationStatus {
    NOT_STARTED, PENDING, IN_PROGRESS, COMPLETED, FAILED, UNKNOWN
  }

  /**
   * Compensation progress information
   */
  @lombok.Data
  @lombok.Builder
  public static class CompensationProgress {
    private final String sagaId;
    private final int totalSteps;
    private final int completedSteps;
    private final int failedSteps;
    private final int pendingSteps;
    private final double progressPercentage;
    private final CompensationStatus status;
    private final List<SagaCompensationEntity> compensations;
  }

  /**
   * Compensation statistics holder
   */
  @lombok.Data
  public static class CompensationStatistics {
    private int attempts = 0;
    private int successes = 0;
    private int failures = 0;
    private long totalDuration = 0;
    private int stepCount = 0;

    public void incrementAttempts() { attempts++; }
    public void incrementSuccesses() { successes++; }
    public void incrementFailures() { failures++; }
    public void addDuration(long duration) { totalDuration += duration; }
    public void setStepCount(int count) { this.stepCount = count; }
    
    public double getSuccessRate() {
      return attempts > 0 ? (double) successes / attempts * 100.0 : 0.0;
    }
    
    public double getAverageDuration() {
      return attempts > 0 ? (double) totalDuration / attempts : 0.0;
    }
  }

  /**
   * Custom exception for compensation errors
   */
  public static class SagaCompensationException extends RuntimeException {
    public SagaCompensationException(String message) {
      super(message);
    }
    
    public SagaCompensationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}