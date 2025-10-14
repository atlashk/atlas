package org.atlas.framework.saga.orchestrator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaCommandReplyHandler;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.entity.SagaCommandEntity;
import org.atlas.framework.saga.entity.SagaCommandStatus;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.entity.SagaStatus;
import org.atlas.framework.saga.exception.SagaCommandNotFoundException;
import org.atlas.framework.saga.exception.SagaConfigException;
import org.atlas.framework.saga.exception.SagaExecutionException;
import org.atlas.framework.saga.exception.SagaNotFoundException;
import org.atlas.framework.saga.messaging.SagaMessagePublisher;
import org.atlas.framework.saga.messaging.payload.SagaCommand;
import org.atlas.framework.saga.messaging.payload.SagaCommandReply;
import org.atlas.framework.saga.messaging.payload.SagaCompensation;
import org.atlas.framework.saga.messaging.payload.SagaCompensationReply;
import org.atlas.framework.saga.repository.SagaCommandRepository;
import org.atlas.framework.saga.repository.SagaRepository;
import org.atlas.framework.util.DateUtil;
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
@Slf4j
public class DefaultSagaOrchestrator implements SagaOrchestrator {

  private final SagaRegistry sagaRegistry;
  private final SagaRepository sagaRepository;
  private final SagaCommandRepository sagaCommandRepository;
  private final SagaMessagePublisher sagaMessagePublisher;

  // Starting saga
  // -----------------------------------------------------------------------------------------------

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Integer startSaga(String sagaName, SagaContext sagaContext) {
    log.info("[SAGA_START] Starting saga execution: sagaName={}, context={}", sagaName,
        sagaContext);

    // Validate orchestrator exists
    if (!sagaRegistry.hasSagaMetadata(sagaName)) {
      throw new IllegalArgumentException("Unknown saga: " + sagaName);
    }

    // Create saga entity
    SagaEntity sagaEntity = SagaEntity.builder()
        .name(sagaName)
        .context(sagaContext.serialize())
        .status(SagaStatus.STARTED)
        .build();
    sagaRepository.insert(sagaEntity);

    // Execute start saga method
    SagaMetadata sagaMetadata = sagaRegistry.getSagaMetadata(sagaName)
        .orElseThrow(
            () -> new SagaConfigException(
                String.format("Orchestrator metadata not found for saga '%s'", sagaName)));
    Method startSagaMethod = sagaMetadata.getStartSagaMethod();
    try {
      startSagaMethod.invoke(sagaMetadata.getSagaBean(), sagaEntity);
    } catch (Exception e) {
      sagaEntity.setStatus(SagaStatus.FAILED);
      throw new SagaExecutionException(String.format("Failed to start saga '%s'", sagaName), e);
    }

    log.info("[SAGA_START] Saga started successfully: sagaId={}, sagaName={}", sagaEntity.getId(),
        sagaName);

    return sagaEntity.getId();
  }

  @Override
  @Transactional
  public void sendCommand(SagaEntity sagaEntity, String sagaCommandName, String targetServiceName) {
    log.debug(
        "[COMMAND_SEND] Sending command: sagaId={}, sagaName={}, sagaCommandName={}, targetServiceName={}",
        sagaEntity.getId(), sagaEntity.getName(), sagaCommandName, targetServiceName);

    // Persist command
    SagaCommandEntity sagaCommandEntity = SagaCommandEntity.builder()
        .sagaId(sagaEntity.getId())
        .name(sagaCommandName)
        .targetServiceName(targetServiceName)
        .status(SagaCommandStatus.STARTED)
        .build();
    sagaCommandRepository.insert(sagaCommandEntity);

    // Publish command message
    SagaCommand command = SagaCommand.builder()
        .sagaId(sagaEntity.getId())
        .sagaName(sagaEntity.getName())
        .sagaContext(sagaEntity.getContext())
        .sagaCommandName(sagaCommandEntity.getName())
        .targetServiceName(targetServiceName)
        .build();
    sagaMessagePublisher.publish(command);

    log.info(
        "[COMMAND_SEND] Command sent successfully: sagaId={}, sagaName={}, sagaCommandId={}, sagaCommandName={}",
        sagaEntity.getId(), sagaEntity.getName(), sagaCommandEntity.getId(),
        sagaCommandEntity.getName());
  }

  @Override
  @Transactional
  public void createCommand(Integer sagaId, String sagaCommandName, String targetServiceName) {
    SagaEntity sagaEntity = findSagaEntity(sagaId);

    log.debug(
        "[COMMAND_CREATE] Creating command: sagaId={}, sagaName={}, sagaCommandName={}, targetServiceName={}",
        sagaId, sagaEntity.getName(), sagaCommandName, targetServiceName);

    // Persist command
    SagaCommandEntity sagaCommandEntity = SagaCommandEntity.builder()
        .sagaId(sagaId)
        .name(sagaCommandName)
        .targetServiceName(targetServiceName)
        .status(SagaCommandStatus.STARTED)
        .build();
    sagaCommandRepository.insert(sagaCommandEntity);
  }

  @Override
  public void endSaga(Integer sagaId) {
    SagaEntity sagaEntity = findSagaEntity(sagaId);

    log.info("[SAGA_END] Ending saga: sagaId={}, sagaName={}, previousStatus={}",
        sagaId, sagaEntity.getName(), sagaEntity.getStatus());

    sagaEntity.setStatus(SagaStatus.COMPLETED);
    sagaRepository.update(sagaEntity);

    log.info("[SAGA_END] Saga completed successfully: sagaId={}, sagaName={}",
        sagaId, sagaEntity.getName());
  }

  @Override
  @Transactional
  public void syncSagaContext(Integer sagaId, SagaContext newSagaContext) {
    SagaEntity sagaEntity = findSagaEntity(sagaId);
    sagaEntity.setContext(newSagaContext.serialize());
    sagaRepository.update(sagaEntity);
  }

  // Command reply handling
  // -----------------------------------------------------------------------------------------------

  @Override
  @Transactional
  public void handleSagaCommandReply(SagaCommandReply reply) {
    log.debug(
        "[COMMAND_REPLY] Processing command reply: sagaId={}, sagaName={}, sagaCommandName={}, success={}",
        reply.getSagaId(), reply.getSagaName(), reply.getSagaCommandName(),
        reply.getResult().isSuccess());

    Optional<SagaMetadata> sagaMetadataOpt = sagaRegistry.getSagaMetadata(reply.getSagaName());
    if (sagaMetadataOpt.isEmpty()) {
      throw new SagaConfigException("Saga metadata not found for saga: " + reply.getSagaName());
    }

    SagaEntity sagaEntity = findSagaEntity(reply.getSagaId());

    SagaCommandEntity sagaCommandEntity =
        findSagaCommandEntity(reply.getSagaId(), reply.getSagaCommandName());

    if (reply.getResult().isSuccess()) {
      // Mark command as COMPLETED
      sagaCommandEntity.setStatus(SagaCommandStatus.COMPLETED);
      sagaCommandEntity.setCompletedAt(DateUtil.now());
      sagaCommandRepository.update(sagaCommandEntity);

      // Trigger command reply handler
      triggerSagaCommandReplyHandler(sagaEntity, sagaCommandEntity, sagaMetadataOpt.get(),
          reply.getResult());
    } else {
      // Mark command as FAILED
      sagaCommandEntity.setStatus(SagaCommandStatus.FAILED);
      sagaCommandEntity.setErrorMessage(reply.getResult().getErrorMessage());
      sagaCommandRepository.update(sagaCommandEntity);

      // Mark saga as FAILED
      sagaEntity.setStatus(SagaStatus.FAILED);
      sagaEntity.setErrorMessage(reply.getResult().getErrorMessage());
      sagaRepository.update(sagaEntity);

      // Compensate saga
      compensateSaga(sagaEntity);
    }
  }

  private void triggerSagaCommandReplyHandler(SagaEntity sagaEntity,
      SagaCommandEntity sagaCommandEntity, SagaMetadata sagaMetadata, Object result) {
    // Find saga command reply handler method
    Method sagaCommandReplyHandlerMethod = sagaMetadata.getSagaCommandReplyHandlerMethods()
        .stream()
        .filter(method -> {
          SagaCommandReplyHandler annotation = method.getAnnotation(SagaCommandReplyHandler.class);
          return annotation != null && annotation.command().equals(sagaCommandEntity.getName());
        })
        .findFirst()
        .orElseThrow(() -> new SagaConfigException(String.format(
            "No command reply handler found for command %s in saga %s",
            sagaCommandEntity.getName(), sagaEntity.getName())));

    try {
      sagaCommandReplyHandlerMethod.invoke(sagaMetadata.getSagaBean(), sagaEntity, result);
    } catch (Exception e) {
      throw new SagaExecutionException(String.format(
          "Failed to execute command reply handler: sagaId=%d, sagaName=%s, sagaCommandId=%d, sagaCommandName=%s",
          sagaEntity.getId(), sagaEntity.getName(), sagaCommandEntity.getId(),
          sagaCommandEntity.getName()), e);
    }
  }

  // Compensation
  // -----------------------------------------------------------------------------------------------

  private void compensateSaga(SagaEntity sagaEntity) {
    log.info("[SAGA_COMPENSATION] Starting compensation process: sagaId={}, sagaName={}",
        sagaEntity.getId(), sagaEntity.getName());

    // Get all completed commands for this saga that need compensation
    List<SagaCommandEntity> completedCommands = sagaCommandRepository.findBySagaId(
            sagaEntity.getId())
        .stream()
        .filter(sagaCommand -> SagaCommandStatus.COMPLETED.equals(sagaCommand.getStatus()))
        .toList();

    if (completedCommands.isEmpty()) {
      log.info(
          "[SAGA_COMPENSATION] No compensation needed: sagaId={}, sagaName={}",
          sagaEntity.getId(), sagaEntity.getName());
      return;
    }

    // Compensate commands in reverse order (LIFO - Last In, First Out)
    for (int i = completedCommands.size() - 1; i >= 0; i--) {
      SagaCommandEntity sagaCommandEntity = completedCommands.get(i);
      compensateCommand(sagaEntity, sagaCommandEntity);
    }

    log.info(
        "[SAGA_COMPENSATION] Compensation process completed: sagaId={}, sagaName={}, compensatedCommands={}",
        sagaEntity.getId(), sagaEntity.getName(), completedCommands.size());
  }

  private void compensateCommand(SagaEntity sagaEntity, SagaCommandEntity sagaCommandEntity) {
    // Mark command as compensating
    sagaCommandEntity.setStatus(SagaCommandStatus.COMPENSATING);
    sagaCommandRepository.update(sagaCommandEntity);

    // Publish compensation message
    SagaCompensation message = SagaCompensation.builder()
        .sagaId(sagaEntity.getId())
        .sagaName(sagaEntity.getName())
        .sagaContext(sagaEntity.getContext())
        .sagaCommandName(sagaCommandEntity.getName())
        .targetServiceName(sagaCommandEntity.getTargetServiceName())
        .build();
    sagaMessagePublisher.publish(message);

    log.info(
        "[COMMAND_COMPENSATION] Compensation event published: sagaId={}, sagaName={}, sagaCommandId={}, sagaCommandName={}",
        sagaEntity.getId(), sagaEntity.getName(), sagaCommandEntity.getId(),
        sagaCommandEntity.getName());
  }

  // Compensation reply handling
  // -----------------------------------------------------------------------------------------------

  @Override
  @Transactional
  public void handleSagaCompensationReply(SagaCompensationReply reply) {
    log.debug(
        "[COMPENSATION_REPLY] Processing compensation reply: sagaId={}, sagaName={}, sagaCommandName={}, success={}",
        reply.getSagaId(), reply.getSagaName(), reply.getSagaCommandName(),
        reply.getResult().isSuccess());

    if (!sagaRegistry.hasSagaMetadata(reply.getSagaName())) {
      throw new SagaConfigException("Saga metadata not found for saga: " + reply.getSagaName());
    }

    findSagaEntity(reply.getSagaId());

    SagaCommandEntity sagaCommandEntity =
        findSagaCommandEntity(reply.getSagaId(), reply.getSagaCommandName());

    if (reply.getResult().isSuccess()) {
      // Mark command as COMPENSATED
      sagaCommandEntity.setStatus(SagaCommandStatus.COMPENSATED);
    } else {
      // Mark command as COMPENSATION_FAILED
      sagaCommandEntity.setStatus(SagaCommandStatus.COMPENSATION_FAILED);
      sagaCommandEntity.setCompensationErrorMessage(reply.getResult().getErrorMessage());
    }
    sagaCommandEntity.setCompletedAt(DateUtil.now());
    sagaCommandRepository.update(sagaCommandEntity);
  }

  // Helper methods
  // -----------------------------------------------------------------------------------------------

  private SagaEntity findSagaEntity(Integer sagaId) {
    return sagaRepository.findById(sagaId)
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));
  }

  private SagaCommandEntity findSagaCommandEntity(Integer sagaId, String sagaCommandName) {
    return sagaCommandRepository.findBySagaIdAndName(sagaId, sagaCommandName)
        .orElseThrow(() -> new SagaCommandNotFoundException(
            String.format("Command %s not found for saga %d", sagaCommandName, sagaId)));
  }
}
