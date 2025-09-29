package org.atlas.framework.saga.orchestrator;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.entity.SagaStatus;
import org.atlas.framework.saga.entity.SagaStepEntity;
import org.atlas.framework.saga.entity.SagaStepStatus;
import org.atlas.framework.saga.event.SagaEventPublisher;
import org.atlas.framework.saga.event.StepCompensationReply;
import org.atlas.framework.saga.event.StepCompensationRequest;
import org.atlas.framework.saga.event.StepExecutionReply;
import org.atlas.framework.saga.event.StepExecutionRequest;
import org.atlas.framework.saga.exception.SagaNotFoundException;
import org.atlas.framework.saga.exception.SagaStepNotFoundException;
import org.atlas.framework.saga.repository.SagaRepository;
import org.atlas.framework.saga.repository.SagaStepRepository;
import org.atlas.framework.util.DateUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event-driven Saga Manager responsible for orchestrating saga execution lifecycle. Uses pure
 * event-driven approach without maintaining in-memory state.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestratorManager {

  private final SagaOrchestratorRegistry orchestratorRegistry;
  private final SagaEventPublisher eventPublisher;
  private final SagaRepository sagaRepository;
  private final SagaStepRepository sagaStepRepository;

  /**
   * Start a new saga with comprehensive validation and error handling
   */
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public CompletableFuture<SagaEntity> startSaga(String orchestratorName,
      Map<String, Object> sagaData) {
    log.info("Starting saga with orchestrator: {}", orchestratorName);

    // Validate orchestrator exists
    if (!orchestratorRegistry.hasOrchestrator(orchestratorName)) {
      throw new IllegalArgumentException("Unknown orchestrator: " + orchestratorName);
    }

    // Create saga entity
    SagaEntity sagaEntity = SagaEntity.builder()
        .orchestratorName(orchestratorName)
        .sagaData(sagaData)
        .sagaStatus(SagaStatus.STARTED)
        .build();
    sagaRepository.insert(sagaEntity);

    // Execute first step
    requestNextStepExecution(sagaEntity);

    log.info("Saga started successfully: {}", sagaEntity.getSagaId());
    return CompletableFuture.completedFuture(sagaEntity);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void executeStep(StepExecutionRequest request) {
    SagaStepEntity stepEntity = sagaStepRepository.findById(request.getStepId())
        .orElseThrow(
            () -> new SagaStepNotFoundException("Saga step not found: " + request.getStepId()));

    SagaEntity sagaEntity = sagaRepository.findById(stepEntity.getSagaId())
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + stepEntity.getSagaId()));

    SagaStepMetadata stepMetadata = orchestratorRegistry.getStep(
        sagaEntity.getOrchestratorName(), stepEntity.getStepName());
    Method method = stepMetadata.getStepMethod();
    try {
      // Invoke the step method
      method.invoke(stepMetadata.getOrchestratorInstance());

      // Publish reply
      StepExecutionReply reply = StepExecutionReply.builder()
          .stepId(stepEntity.getStepId())
          .success(true)
          .build();
      eventPublisher.publish(reply);
    } catch (Exception e) {
      log.error("Saga step execution failed: {}, error: {}", stepEntity.getStepId(),
          e.getMessage());
      StepExecutionReply reply = StepExecutionReply.builder()
          .stepId(stepEntity.getStepId())
          .success(false)
          .errorMessage(e.getMessage())
          .build();
      eventPublisher.publish(reply);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void executeCompensation(StepCompensationRequest request) {
    SagaStepEntity stepEntity = sagaStepRepository.findById(request.getStepId())
        .orElseThrow(
            () -> new SagaStepNotFoundException("Saga step not found: " + request.getStepId()));

    SagaEntity sagaEntity = sagaRepository.findById(stepEntity.getSagaId())
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + stepEntity.getSagaId()));

    SagaStepMetadata stepMetadata = orchestratorRegistry.getStep(
        sagaEntity.getOrchestratorName(), stepEntity.getStepName());
    Method compensateMethod = stepMetadata.getCompensateMethod();
    try {
      // Invoke the step method
      compensateMethod.invoke(stepMetadata.getOrchestratorInstance());

      // Publish reply
      StepCompensationReply reply = StepCompensationReply.builder()
          .stepId(stepEntity.getStepId())
          .success(true)
          .build();
      eventPublisher.publish(reply);
    } catch (Exception e) {
      log.error("Saga step compensation execution failed: {}, error: {}", stepEntity.getStepId(),
          e.getMessage());
      StepCompensationReply reply = StepCompensationReply.builder()
          .stepId(stepEntity.getStepId())
          .success(false)
          .errorMessage(e.getMessage())
          .build();
      eventPublisher.publish(reply);
    }
  }

  @Transactional
  public void onStepSucceeded(StepExecutionReply reply) {
    SagaStepEntity stepEntity = sagaStepRepository.findById(reply.getStepId())
        .orElseThrow(
            () -> new SagaStepNotFoundException("Saga step not found: " + reply.getStepId()));

    SagaEntity sagaEntity = sagaRepository.findById(stepEntity.getSagaId())
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + stepEntity.getSagaId()));

    // Mark step as COMPLETED
    stepEntity.setStepStatus(SagaStepStatus.COMPLETED);
    stepEntity.setCompletedAt(DateUtil.now());
    sagaStepRepository.update(stepEntity);

    // Check for next step
    Optional<SagaStepEntity> lastStepEntityOpt = sagaStepRepository.findLastStep(
        sagaEntity.getSagaId());
    if (lastStepEntityOpt.isPresent()) {
      SagaStepEntity lastStepEntity = lastStepEntityOpt.get();
      SagaStepMetadata nextStepMetadata = orchestratorRegistry.getNextStep(
          sagaEntity.getOrchestratorName(), lastStepEntity.getStepName());
      if (nextStepMetadata != null) {
        // Execute next step
        requestNextStepExecution(sagaEntity);
      } else {
        // No more steps, mark saga as COMPLETED
        sagaEntity.setSagaStatus(SagaStatus.COMPLETED);
        sagaEntity.setCompletedAt(DateUtil.now());
        sagaRepository.update(sagaEntity);
        log.info("Saga completed successfully: {}", sagaEntity.getSagaId());
      }
    } else {
      log.warn("No last step found for saga: {}", sagaEntity.getSagaId());
    }
  }

  @Transactional
  public void onStepFailed(StepExecutionReply reply) {
    SagaStepEntity stepEntity = sagaStepRepository.findById(reply.getStepId())
        .orElseThrow(
            () -> new SagaStepNotFoundException("Saga step not found: " + reply.getStepId()));

    SagaEntity sagaEntity = sagaRepository.findById(stepEntity.getSagaId())
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + stepEntity.getSagaId()));

    // Mark step as FAILED
    stepEntity.setStepStatus(SagaStepStatus.FAILED);
    stepEntity.setErrorMessage(reply.getErrorMessage());
    sagaStepRepository.update(stepEntity);

    // Mark saga as FAILED
    sagaEntity.setSagaStatus(SagaStatus.FAILED);
    sagaEntity.setCompletedAt(DateUtil.now());
    sagaRepository.update(sagaEntity);

    // Trigger compensation for completed steps
    requestStepCompensation(sagaEntity);
  }

  @Transactional
  public void onCompensationSucceeded(StepCompensationReply reply) {
    SagaStepEntity stepEntity = sagaStepRepository.findById(reply.getStepId())
        .orElseThrow(
            () -> new SagaStepNotFoundException("Saga step not found: " + reply.getStepId()));

    // Mark step as COMPENSATED
    stepEntity.setStepStatus(SagaStepStatus.COMPENSATED);
    sagaStepRepository.update(stepEntity);
  }

  @Transactional
  public void onCompensationFailed(StepCompensationReply reply) {
    SagaStepEntity stepEntity = sagaStepRepository.findById(reply.getStepId())
        .orElseThrow(
            () -> new SagaStepNotFoundException("Saga step not found: " + reply.getStepId()));

    // Mark step as COMPENSATION_FAILED
    stepEntity.setStepStatus(SagaStepStatus.COMPENSATION_FAILED);
    stepEntity.setCompensationErrorMessage(reply.getErrorMessage());
    stepEntity.setCompletedAt(DateUtil.now());
    sagaStepRepository.update(stepEntity);
  }

  // Private helper methods

  /**
   * Execute the next step in the saga with enhanced error handling
   */
  private void requestNextStepExecution(SagaEntity sagaEntity) {
    Optional<SagaStepEntity> lastStepEntityOpt = sagaStepRepository.findLastStep(
        sagaEntity.getSagaId());
    if (lastStepEntityOpt.isEmpty()) {
      // No steps executed yet, start with the first step
      SagaStepMetadata firstStepMetadata = orchestratorRegistry.getFirstStep(
          sagaEntity.getOrchestratorName());
      requestStepExecution(sagaEntity, firstStepMetadata);
    } else {
      SagaStepEntity lastStepEntity = lastStepEntityOpt.get();
      SagaStepMetadata nextStepMetadata = orchestratorRegistry.getNextStep(
          sagaEntity.getOrchestratorName(), lastStepEntity.getStepName());
      if (nextStepMetadata != null) {
        requestStepExecution(sagaEntity, nextStepMetadata);
      } else {
        log.warn("No next step found for saga: {}", sagaEntity.getSagaId());
      }
    }
  }

  private void requestStepExecution(SagaEntity sagaEntity, SagaStepMetadata stepMetadata) {
    // Insert new step entity
    SagaStepEntity stepEntity = SagaStepEntity.builder()
        .sagaId(sagaEntity.getSagaId())
        .stepName(stepMetadata.getStepName())
        .stepOrder(1)
        .stepStatus(SagaStepStatus.STARTED)
        .build();
    sagaStepRepository.insert(stepEntity);

    // Publish request
    StepExecutionRequest request = StepExecutionRequest.builder()
        .stepId(stepEntity.getStepId())
        .build();
    eventPublisher.publish(request);

    log.info("Requested execution for saga: {}, step: {}", sagaEntity.getSagaId(),
        stepMetadata.getStepName());
  }

  private void requestStepCompensation(SagaEntity sagaEntity) {
    List<SagaStepEntity> completedSteps = sagaStepRepository.findCompleted(sagaEntity.getSagaId());
    for (int i = completedSteps.size() - 1; i >= 0; i--) {
      SagaStepEntity stepEntity = completedSteps.get(i);
      SagaStepMetadata stepMetadata = orchestratorRegistry.getStep(
          sagaEntity.getOrchestratorName(), stepEntity.getStepName());
      Method compensateMethod = stepMetadata.getCompensateMethod();
      if (compensateMethod != null) {
        // Mark step as COMPENSATING
        stepEntity.setStepStatus(SagaStepStatus.COMPENSATING);
        sagaStepRepository.update(stepEntity);

        // Publish request
        StepCompensationRequest request = StepCompensationRequest.builder()
            .stepId(stepEntity.getStepId())
            .build();
        eventPublisher.publish(request);
        log.info("Requested compensation for saga: {}, step: {}", sagaEntity.getSagaId(),
            stepEntity.getStepName());
      } else {
        log.warn("No compensation method defined for step: {}", stepEntity.getStepName());
      }
    }
  }
}
