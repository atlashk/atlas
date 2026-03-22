package org.atlas.libs.framework.saga.core.orchestrator;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.saga.core.annotation.SagaCommandReplyHandler;
import org.atlas.libs.framework.saga.core.command.SagaCommandResult;
import org.atlas.libs.framework.saga.core.context.SagaContext;
import org.atlas.libs.framework.saga.core.entity.SagaCommandEntity;
import org.atlas.libs.framework.saga.core.entity.SagaCommandStatus;
import org.atlas.libs.framework.saga.core.entity.SagaEntity;
import org.atlas.libs.framework.saga.core.entity.SagaStatus;
import org.atlas.libs.framework.saga.core.exception.SagaCommandNotFoundException;
import org.atlas.libs.framework.saga.core.exception.SagaConfigException;
import org.atlas.libs.framework.saga.core.exception.SagaExecutionException;
import org.atlas.libs.framework.saga.core.exception.SagaNotFoundException;
import org.atlas.libs.framework.saga.core.messaging.SagaMessagePublisher;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommand;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCommandReply;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCompensation;
import org.atlas.libs.framework.saga.core.messaging.payload.SagaCompensationReply;
import org.atlas.libs.framework.saga.core.repository.SagaCommandRepository;
import org.atlas.libs.framework.saga.core.repository.SagaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event-driven Saga Manager responsible for orchestrating saga execution lifecycle. Uses pure
 * event-driven approach without maintaining in-memory state.
 */
@Component
@ConditionalOnBean(SagaRepository.class)
@RequiredArgsConstructor
@Slf4j(topic = "saga.orchestrator")
public class DefaultSagaOrchestrator implements SagaOrchestrator {

  private final SagaRegistry sagaRegistry;
  private final SagaRepository sagaRepository;
  private final SagaCommandRepository sagaCommandRepository;
  private final SagaMessagePublisher sagaMessagePublisher;

  // Starting saga
  // -----------------------------------------------------------------------------------------------

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Integer runSaga(String sagaName, SagaContext sagaContext) {
    log.info("Started saga execution: sagaName={}, context={}", sagaName, sagaContext);

    // Validate orchestrator exists
    if (!sagaRegistry.hasSagaMetadata(sagaName)) {
      throw new IllegalArgumentException("Unknown saga: " + sagaName);
    }

    // Create saga entity
    SagaEntity saga = SagaEntity.builder()
        .name(sagaName)
        .context(sagaContext.serialize())
        .status(SagaStatus.STARTED)
        .build();
    sagaRepository.insert(saga);

    // Execute start saga method
    SagaMetadata sagaMetadata = sagaRegistry.getSagaMetadata(sagaName)
        .orElseThrow(
            () -> new SagaConfigException(
                String.format("Orchestrator metadata not found for saga '%s'", sagaName)));
    Method startSagaMethod = sagaMetadata.getStartSagaMethod();
    try {
      startSagaMethod.invoke(sagaMetadata.getSagaBean(), saga);
    } catch (Exception e) {
      saga.setStatus(SagaStatus.FAILED);
      throw new SagaExecutionException(String.format("Failed to start saga '%s'", sagaName), e);
    }

    log.info("[SAGA_START] Saga started successfully: sagaId={}, sagaName={}", saga.getId(),
        sagaName);

    return saga.getId();
  }

  @Override
  @Transactional
  public void sendSagaCommand(SagaEntity saga, String sagaCommandName, String targetServiceName) {
    log.debug(
        "Sending command: sagaId={}, sagaName={}, sagaCommandName={}, targetServiceName={}",
        saga.getId(), saga.getName(), sagaCommandName, targetServiceName);

    // Persist saga command
    SagaCommandEntity sagaCommand = SagaCommandEntity.builder()
        .sagaId(saga.getId())
        .name(sagaCommandName)
        .targetServiceName(targetServiceName)
        .status(SagaCommandStatus.STARTED)
        .build();
    sagaCommandRepository.insert(sagaCommand);

    // Publish saga command message
    SagaCommand payload = SagaCommand.builder()
        .sagaId(saga.getId())
        .sagaName(saga.getName())
        .sagaContext(saga.getContext())
        .sagaCommandName(sagaCommand.getName())
        .targetServiceName(targetServiceName)
        .build();
    sagaMessagePublisher.publish(payload);

    log.info(
        "Saga command sent successfully: sagaId={}, sagaName={}, sagaCommandId={}, sagaCommandName={}",
        saga.getId(), saga.getName(), sagaCommand.getId(),
        sagaCommand.getName());
  }

  @Override
  @Transactional
  public void createSagaCommand(Integer sagaId, String sagaCommandName, String targetServiceName) {
    SagaEntity saga = findSaga(sagaId);

    log.debug(
        "Creating command: sagaId={}, sagaName={}, sagaCommandName={}, targetServiceName={}",
        sagaId, saga.getName(), sagaCommandName, targetServiceName);

    // Persist command
    SagaCommandEntity sagaCommand = SagaCommandEntity.builder()
        .sagaId(sagaId)
        .name(sagaCommandName)
        .targetServiceName(targetServiceName)
        .status(SagaCommandStatus.STARTED)
        .build();
    sagaCommandRepository.insert(sagaCommand);
  }

  @Override
  @Transactional
  public void rollbackSaga(Integer sagaId) {
    SagaEntity saga = findSaga(sagaId);

    log.info("Started manual rollback: sagaId={}, sagaName={}, currentStatus={}",
        sagaId, saga.getName(), saga.getStatus());

    saga.setStatus(SagaStatus.FAILED);
    sagaRepository.update(saga);
    compensateSaga(saga);

    log.info("Manual rollback published compensation events: sagaId={}, sagaName={}",
        sagaId, saga.getName());
  }

  @Override
  public void endSaga(Integer sagaId) {
    SagaEntity saga = findSaga(sagaId);

    log.info("Ending saga: sagaId={}, sagaName={}, previousStatus={}",
        sagaId, saga.getName(), saga.getStatus());

    saga.setStatus(SagaStatus.COMPLETED);
    sagaRepository.update(saga);

    log.info("Saga completed successfully: sagaId={}, sagaName={}",
        sagaId, saga.getName());
  }

  // Command reply handling
  // -----------------------------------------------------------------------------------------------

  @Override
  @Transactional
  public void handleSagaCommandReply(SagaCommandReply sagaCommandReply) {
    log.debug(
        "Processing command reply: sagaId={}, sagaName={}, sagaCommandName={}, success={}",
        sagaCommandReply.getSagaId(), sagaCommandReply.getSagaName(),
        sagaCommandReply.getSagaCommandName(), sagaCommandReply.getSagaCommandResult().isSuccess());

    Optional<SagaMetadata> sagaMetadataOpt = sagaRegistry.getSagaMetadata(
        sagaCommandReply.getSagaName());
    if (sagaMetadataOpt.isEmpty()) {
      throw new SagaConfigException(
          "Saga metadata not found for saga: " + sagaCommandReply.getSagaName());
    }

    SagaEntity saga = findSaga(sagaCommandReply.getSagaId());

    SagaCommandEntity sagaCommand = findSagaCommand(
        sagaCommandReply.getSagaId(), sagaCommandReply.getSagaCommandName());

    // Trigger command reply handler if exists
    triggerSagaCommandReplyHandler(
        saga, sagaCommand, sagaMetadataOpt.get(), sagaCommandReply.getSagaCommandResult());

    if (sagaCommandReply.getSagaCommandResult().isSuccess()) {
      // Mark command as COMPLETED
      sagaCommand.setStatus(SagaCommandStatus.COMPLETED);
      sagaCommand.setCompletedAt(LocalDateTime.now());
      sagaCommandRepository.update(sagaCommand);
    } else {
      // Mark command as FAILED
      sagaCommand.setStatus(SagaCommandStatus.FAILED);
      sagaCommand.setError(sagaCommandReply.getSagaCommandResult().getError());
      sagaCommandRepository.update(sagaCommand);

      // Mark saga as FAILED
      saga.setStatus(SagaStatus.FAILED);
      saga.setErrorMessage(sagaCommandReply.getSagaCommandResult().getError());
      sagaRepository.update(saga);

      // Compensate saga
      compensateSaga(saga);
    }
  }

  private void triggerSagaCommandReplyHandler(SagaEntity saga,
      SagaCommandEntity sagaCommand, SagaMetadata sagaMetadata,
      SagaCommandResult sagaCommandResult) {
    // Find saga command reply handler method
    sagaMetadata.getSagaCommandReplyHandlerMethods()
        .stream()
        .filter(method -> {
          SagaCommandReplyHandler annotation = method.getAnnotation(SagaCommandReplyHandler.class);
          return annotation != null && annotation.command().equals(sagaCommand.getName());
        })
        .findFirst()
        .ifPresent(sagaCommandReplyHandlerMethod -> {
          try {
            sagaCommandReplyHandlerMethod.invoke(
                sagaMetadata.getSagaBean(), saga, sagaCommandResult);
          } catch (Exception e) {
            throw new SagaExecutionException(String.format(
                "Failed to execute command reply handler: sagaId=%d, sagaName=%s, sagaCommandId=%d, sagaCommandName=%s",
                saga.getId(), saga.getName(), sagaCommand.getId(),
                sagaCommand.getName()), e);
          }
        });
  }

  // Compensation
  // -----------------------------------------------------------------------------------------------

  private void compensateSaga(SagaEntity saga) {
    log.info("Started compensation process: sagaId={}, sagaName={}", saga.getId(), saga.getName());

    // Get all completed commands for this saga that need compensation
    List<SagaCommandEntity> completedCommands = sagaCommandRepository.findBySagaId(
            saga.getId())
        .stream()
        .filter(sagaCommand -> SagaCommandStatus.COMPLETED.equals(sagaCommand.getStatus()))
        .toList();

    if (completedCommands.isEmpty()) {
      log.info("No compensation needed: sagaId={}, sagaName={}", saga.getId(), saga.getName());
      return;
    }

    // Compensate commands in reverse order (LIFO - Last In, First Out)
    for (int i = completedCommands.size() - 1; i >= 0; i--) {
      SagaCommandEntity sagaCommand = completedCommands.get(i);
      compensateCommand(saga, sagaCommand);
    }

    log.info(
        "Compensation process completed: sagaId={}, sagaName={}, compensatedCommands={}",
        saga.getId(), saga.getName(), completedCommands.size());
  }

  private void compensateCommand(SagaEntity saga, SagaCommandEntity sagaCommand) {
    // Mark command as compensating
    sagaCommand.setStatus(SagaCommandStatus.COMPENSATING);
    sagaCommandRepository.update(sagaCommand);

    // Publish compensation message
    SagaCompensation message = SagaCompensation.builder()
        .sagaId(saga.getId())
        .sagaName(saga.getName())
        .sagaContext(saga.getContext())
        .sagaCommandName(sagaCommand.getName())
        .targetServiceName(sagaCommand.getTargetServiceName())
        .build();
    sagaMessagePublisher.publish(message);

    log.info(
        "Compensation event published: sagaId={}, sagaName={}, sagaCommandId={}, sagaCommandName={}",
        saga.getId(), saga.getName(), sagaCommand.getId(), sagaCommand.getName());
  }

  // Compensation reply handling
  // -----------------------------------------------------------------------------------------------

  @Override
  @Transactional
  public void handleSagaCompensationReply(SagaCompensationReply sagaCompensationReply) {
    log.debug(
        "Processing compensation reply: sagaId={}, sagaName={}, sagaCommandName={}, success={}",
        sagaCompensationReply.getSagaId(), sagaCompensationReply.getSagaName(),
        sagaCompensationReply.getSagaCommandName(), sagaCompensationReply.getResult().isSuccess());

    if (!sagaRegistry.hasSagaMetadata(sagaCompensationReply.getSagaName())) {
      throw new SagaConfigException(
          "Saga metadata not found for saga: " + sagaCompensationReply.getSagaName());
    }

    findSaga(sagaCompensationReply.getSagaId());

    SagaCommandEntity sagaCommand = findSagaCommand(sagaCompensationReply.getSagaId(),
        sagaCompensationReply.getSagaCommandName());

    if (sagaCompensationReply.getResult().isSuccess()) {
      // Mark command as COMPENSATED
      sagaCommand.setStatus(SagaCommandStatus.COMPENSATED);
    } else {
      // Mark command as COMPENSATION_FAILED
      sagaCommand.setStatus(SagaCommandStatus.COMPENSATION_FAILED);
      sagaCommand.setCompensationError(sagaCompensationReply.getResult().getError());
    }
    sagaCommand.setCompletedAt(LocalDateTime.now());
    sagaCommandRepository.update(sagaCommand);
  }

  // Helper methods
  // -----------------------------------------------------------------------------------------------

  private SagaEntity findSaga(Integer sagaId) {
    return sagaRepository.findById(sagaId)
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));
  }

  private SagaCommandEntity findSagaCommand(Integer sagaId, String sagaCommandName) {
    return sagaCommandRepository.findBySagaIdAndName(sagaId, sagaCommandName)
        .orElseThrow(() -> new SagaCommandNotFoundException(
            String.format("Command %s not found for saga %d", sagaCommandName, sagaId)));
  }
}
