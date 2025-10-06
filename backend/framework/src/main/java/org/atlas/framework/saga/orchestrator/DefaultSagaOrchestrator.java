package org.atlas.framework.saga.orchestrator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.saga.annotation.SagaCommandReplyHandler;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.entity.SagaCommandEntity;
import org.atlas.framework.saga.entity.SagaCommandStatus;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.entity.SagaStatus;
import org.atlas.framework.saga.event.SagaCommandEvent;
import org.atlas.framework.saga.event.SagaCommandReplyEvent;
import org.atlas.framework.saga.event.SagaCompensationEvent;
import org.atlas.framework.saga.event.SagaCompensationReplyEvent;
import org.atlas.framework.saga.event.SagaEventPublisherPort;
import org.atlas.framework.saga.exception.SagaCommandNotFoundException;
import org.atlas.framework.saga.exception.SagaConfigException;
import org.atlas.framework.saga.exception.SagaExecutionException;
import org.atlas.framework.saga.exception.SagaNotFoundException;
import org.atlas.framework.saga.repository.SagaCommandRepository;
import org.atlas.framework.saga.repository.SagaRepository;
import org.atlas.framework.util.DateUtil;
import org.atlas.framework.util.StringUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event-driven Saga Manager responsible for orchestrating saga execution lifecycle. Uses pure
 * event-driven approach without maintaining in-memory state.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultSagaOrchestrator implements SagaOrchestrator {

  private final SagaRegistry sagaRegistry;
  private final SagaRepository sagaRepository;
  private final SagaCommandRepository sagaCommandRepository;
  private final SagaEventPublisherPort sagaEventPublisherPort;
  private final ApplicationConfigService applicationConfigService;

  // Starting saga
  // -----------------------------------------------------------------------------------------------

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public Long startSaga(String sagaName, SagaContext sagaContext) {
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
            () -> new SagaConfigException("Orchestrator metadata not found for saga: " + sagaName));
    Method startSagaMethod = sagaMetadata.getStartSagaMethod();
    try {
      startSagaMethod.invoke(sagaMetadata.getSagaBean(), sagaContext);
    } catch (IllegalAccessException | InvocationTargetException e) {
      sagaEntity.setStatus(SagaStatus.FAILED);
      throw new SagaExecutionException("Failed to start saga: " + sagaName, e);
    }

    log.info("[SAGA_START] Saga started successfully: sagaId={}, sagaName={}", sagaEntity.getId(),
        sagaName);

    return sagaEntity.getId();
  }

  @Override
  @Transactional
  public void sendCommand(Long sagaId, String sagaCommandName) {
    SagaEntity sagaEntity = sagaRepository.findById(sagaId)
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));

    log.debug("[COMMAND_SEND] Sending command: sagaId={}, sagaName={}, sagaCommandName={}",
        sagaId, sagaEntity.getName(), sagaCommandName);

    // Persist command
    SagaCommandEntity sagaCommandEntity = SagaCommandEntity.builder()
        .sagaId(sagaId)
        .name(sagaCommandName)
        .status(SagaCommandStatus.STARTED)
        .build();
    sagaCommandRepository.insert(sagaCommandEntity);

    // Publish command
    SagaCommandEvent event = new SagaCommandEvent(applicationConfigService.getApplicationName());
    event.setSagaId(sagaId);
    event.setSagaCommandName(sagaCommandEntity.getName());
    event.setSagaContext(sagaEntity.getContext());
    sagaEventPublisherPort.publish(event);

    log.info(
        "[COMMAND_SEND] Command sent successfully: sagaId={}, sagaName={}, sagaCommandId={}, sagaCommandName={}",
        sagaId, sagaEntity.getName(), sagaCommandEntity.getId(), sagaCommandEntity.getName());
  }

  @Override
  public void endSaga(Long sagaId) {
    SagaEntity sagaEntity = sagaRepository.findById(sagaId)
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));

    log.info("[SAGA_END] Ending saga: sagaId={}, sagaName={}, previousStatus={}",
        sagaId, sagaEntity.getName(), sagaEntity.getStatus());

    sagaEntity.setStatus(SagaStatus.COMPLETED);
    sagaRepository.update(sagaEntity);

    log.info("[SAGA_END] Saga completed successfully: sagaId={}, sagaName={}",
        sagaId, sagaEntity.getName());
  }

  // Command reply handling
  // -----------------------------------------------------------------------------------------------

  @Override
  @Transactional
  public void handleSagaCommandReplyEvent(SagaCommandReplyEvent event) {
    log.debug(
        "[COMMAND_REPLY] Processing command reply: sagaId={}, sagaName={}, sagaCommandName={}, success={}",
        event.getSagaId(), event.getSagaName(), event.getSagaCommandName(), event.isSuccess());

    Optional<SagaMetadata> sagaMetadataOpt = sagaRegistry.getSagaMetadata(event.getSagaName());
    if (sagaMetadataOpt.isEmpty()) {
      throw new SagaConfigException("Saga metadata not found for saga: " + event.getSagaName());
    }

    SagaEntity sagaEntity = sagaRepository.findById(event.getSagaId())
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + event.getSagaId()));

    SagaCommandEntity sagaCommandEntity =
        sagaCommandRepository.findBySagaIdAndName(event.getSagaId(), event.getSagaCommandName())
            .orElseThrow(() -> new SagaCommandNotFoundException(
                String.format("Command %s not found for saga %d",
                    event.getSagaCommandName(), event.getSagaId())));

    if (event.isSuccess()) {
      // Mark command as COMPLETED
      sagaCommandEntity.setStatus(SagaCommandStatus.COMPLETED);
      sagaCommandEntity.setCompletedAt(DateUtil.now());
      sagaCommandRepository.update(sagaCommandEntity);

      // Trigger command reply handler
      triggerSagaCommandReplyHandler(sagaEntity, sagaCommandEntity, sagaMetadataOpt.get(),
          event.getResult());
    } else {
      // Mark command as FAILED
      sagaCommandEntity.setStatus(SagaCommandStatus.FAILED);
      sagaCommandEntity.setErrorMessage(StringUtil.sanitizeErrorMessage(event.getErrorMessage()));
      sagaCommandRepository.update(sagaCommandEntity);

      // Mark saga as FAILED
      sagaEntity.setStatus(SagaStatus.FAILED);
      sagaEntity.setErrorMessage(StringUtil.sanitizeErrorMessage(event.getErrorMessage()));
      sagaRepository.update(sagaEntity);

      // Compensate saga
      compensateSaga(sagaEntity);
    }
  }

  private void triggerSagaCommandReplyHandler(SagaEntity sagaEntity,
      SagaCommandEntity sagaCommandEntity, SagaMetadata sagaMetadata, Object result) {
    // Deserialize saga context
    SagaContext sagaContext = SagaContext.deserialize(sagaEntity.getContext());

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
      sagaCommandReplyHandlerMethod.invoke(sagaMetadata.getSagaBean(), sagaContext, result);
    } catch (IllegalAccessException | InvocationTargetException e) {
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

    // Deserialize saga context
    SagaContext sagaContext = SagaContext.deserialize(sagaEntity.getContext());
    sagaContext.setSagaId(sagaEntity.getId());

    // Compensate commands in reverse order (LIFO - Last In, First Out)
    for (int i = completedCommands.size() - 1; i >= 0; i--) {
      SagaCommandEntity sagaCommandEntity = completedCommands.get(i);
      compensateCommand(sagaEntity, sagaCommandEntity, sagaContext);
    }

    log.info(
        "[SAGA_COMPENSATION] Compensation process completed: sagaId={}, sagaName={}, compensatedCommands={}",
        sagaEntity.getId(), sagaEntity.getName(), completedCommands.size());
  }

  private void compensateCommand(SagaEntity sagaEntity, SagaCommandEntity sagaCommandEntity,
      SagaContext sagaContext) {
    // Mark command as compensating
    sagaCommandEntity.setStatus(SagaCommandStatus.COMPENSATING);
    sagaCommandRepository.update(sagaCommandEntity);

    // Publish compensation as event
    SagaCompensationEvent event = new SagaCompensationEvent(
        applicationConfigService.getApplicationName());
    event.setSagaId(sagaEntity.getId());
    event.setSagaName(sagaEntity.getName());
    event.setSagaCommandName(sagaCommandEntity.getName());
    event.setSagaContext(sagaContext.serialize());
    sagaEventPublisherPort.publish(event);

    log.info(
        "[COMMAND_COMPENSATION] Compensation event published: sagaId={}, sagaName={}, sagaCommandId={}, sagaCommandName={}",
        sagaEntity.getId(), sagaEntity.getName(), sagaCommandEntity.getId(),
        sagaCommandEntity.getName());
  }

  // Compensation reply handling
  // -----------------------------------------------------------------------------------------------

  @Override
  @Transactional
  public void handleSagaCompensationReplyEvent(SagaCompensationReplyEvent event) {
    log.debug(
        "[COMPENSATION_REPLY] Processing compensation reply: sagaId={}, sagaName={}, sagaCommandName={}, success={}",
        event.getSagaId(), event.getSagaName(), event.getSagaCommandName(), event.isSuccess());

    if (!sagaRegistry.hasSagaMetadata(event.getSagaName())) {
      throw new SagaConfigException("Saga metadata not found for saga: " + event.getSagaName());
    }

    sagaRepository.findById(event.getSagaId())
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + event.getSagaId()));

    SagaCommandEntity sagaCommandEntity =
        sagaCommandRepository.findBySagaIdAndName(event.getSagaId(), event.getSagaCommandName())
            .orElseThrow(() -> new SagaCommandNotFoundException(
                String.format("Command %s not found for saga %d",
                    event.getSagaCommandName(), event.getSagaId())));

    if (event.isSuccess()) {
      // Mark command as COMPENSATED
      sagaCommandEntity.setStatus(SagaCommandStatus.COMPENSATED);
    } else {
      // Mark command as COMPENSATION_FAILED
      sagaCommandEntity.setStatus(SagaCommandStatus.COMPENSATION_FAILED);
      sagaCommandEntity.setErrorMessage(StringUtil.sanitizeErrorMessage(event.getErrorMessage()));
    }
    sagaCommandEntity.setCompletedAt(DateUtil.now());
    sagaCommandRepository.update(sagaCommandEntity);
  }
}
