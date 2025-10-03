package org.atlas.framework.saga.lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.config.ApplicationConfigService;
import org.atlas.framework.saga.event.SagaCommandEvent;
import org.atlas.framework.saga.command.SagaCommandType;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.entity.SagaStatus;
import org.atlas.framework.saga.entity.SagaCommandEntity;
import org.atlas.framework.saga.entity.SagaCommandStatus;
import org.atlas.framework.saga.event.SagaCommandReplyEvent;
import org.atlas.framework.saga.event.SagaEventPublisher;
import org.atlas.framework.saga.exception.SagaCommandNotFoundException;
import org.atlas.framework.saga.exception.SagaConfigException;
import org.atlas.framework.saga.exception.SagaExecutionException;
import org.atlas.framework.saga.exception.SagaNotFoundException;
import org.atlas.framework.saga.orchestrator.SagaOrchestratorMetadata;
import org.atlas.framework.saga.orchestrator.SagaOrchestratorRegistry;
import org.atlas.framework.saga.repository.SagaRepository;
import org.atlas.framework.saga.repository.SagaCommandRepository;
import org.atlas.framework.util.DateUtil;
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
public class DefaultSagaLifecycle implements SagaLifecycle {

  private final SagaOrchestratorRegistry sagaOrchestratorRegistry;
  private final SagaRepository sagaRepository;
  private final SagaCommandRepository sagaCommandRepository;
  private final SagaEventPublisher sagaEventPublisher;

  // Starting saga
  // -----------------------------------------------------------------------------------------------

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public void startSaga(String sagaName, SagaContext sagaContext) {
    log.info("Starting saga {} with context: {}", sagaName, sagaContext);

    // Validate orchestrator exists
    if (!sagaOrchestratorRegistry.hasOrchestrator(sagaName)) {
      throw new IllegalArgumentException("Unknown orchestrator for saga " + sagaName);
    }

    // Create saga entity
    SagaEntity sagaEntity = SagaEntity.builder()
        .name(sagaName)
        .context(sagaContext.serialize())
        .status(SagaStatus.STARTED)
        .build();
    sagaRepository.insert(sagaEntity);

    // Execute start saga method
    SagaOrchestratorMetadata orchestratorMetadata = sagaOrchestratorRegistry.getOrchestrator(
            sagaName)
        .orElseThrow(
            () -> new SagaConfigException("Orchestrator metadata not found for saga " + sagaName));
    Method startSagaMethod = orchestratorMetadata.getStartSagaMethod();
    try {
      startSagaMethod.invoke(orchestratorMetadata.getOrchestratorInstance(), sagaContext);
    } catch (IllegalAccessException | InvocationTargetException e) {
      sagaEntity.setStatus(SagaStatus.FAILED);
      throw new SagaExecutionException("Failed to start saga" + sagaName, e);
    }

    log.info("Saga started successfully: {}", sagaEntity.getId());
  }

  @Override
  @Transactional
  public void sendCommand(Long sagaId, SagaCommandType sagaCommandType) {
    SagaEntity sagaEntity = sagaRepository.findById(sagaId)
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));

    // Persist command
    SagaCommandEntity sagaCommandEntity = SagaCommandEntity.builder()
        .sagaId(sagaId)
        .name(sagaCommandType.name())
        .status(SagaCommandStatus.STARTED)
        .build();
    sagaCommandRepository.insert(sagaCommandEntity);

    // Publish command
    SagaCommandEvent sagaCommandEvent = SagaCommandEvent.builder()
        .sagaId(sagaId)
        .commandName(sagaCommandEntity.getName())
        .sagaContext(sagaEntity.getContext())
        .build();
    sagaEventPublisher.publish(sagaCommandEvent);
  }

  @Override
  public void endSaga(Long sagaId) {
    SagaEntity sagaEntity = sagaRepository.findById(sagaId)
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));
    sagaEntity.setStatus(SagaStatus.COMPLETED);
    sagaRepository.update(sagaEntity);
  }

  // Command reply handlers
  // -----------------------------------------------------------------------------------------------

  @Transactional
  public void handleSagaCommandReplyEvent(SagaCommandReplyEvent event) {
    SagaEntity sagaEntity = sagaRepository.findById(event.getSagaId())
        .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + event.getSagaId()));

    SagaCommandEntity sagaCommandEntity =
        sagaCommandRepository.findBySagaIdAndName(event.getSagaId(), event.getCommandName())
            .orElseThrow(() -> new SagaCommandNotFoundException(
                String.format("Command %s not found for saga %d",
                    event.getCommandName(), event.getSagaId())));

    if (event.isSuccess()) {
      // Mark command as COMPLETED
      sagaCommandEntity.setStatus(SagaCommandStatus.COMPLETED);
      sagaCommandEntity.setCompletedAt(DateUtil.now());
      sagaCommandRepository.update(sagaCommandEntity);

      // Trigger command reply handler
    } else {
      // Mark command as FAILED
      sagaCommandEntity.setStatus(SagaCommandStatus.FAILED);
      sagaCommandEntity.setErrorMessage(event.getErrorMessage());
      sagaCommandRepository.update(sagaCommandEntity);

      // Mark saga as FAILED
      sagaEntity.setStatus(SagaStatus.FAILED);
      sagaEntity.setErrorMessage(event.getErrorMessage());
      sagaRepository.update(sagaEntity);
    }
  }

  private void triggerSagaCommandReplyHandler(SagaEntity sagaEntity,
      SagaCommandEntity sagaCommandEntity) {
    SagaOrchestratorMetadata sagaOrchestratorMetadata = sagaOrchestratorRegistry.getOrchestrator(
            sagaEntity.getName())
        .orElseThrow(
            () -> new SagaConfigException(
                "Orchestrator metadata not found for saga " + sagaEntity.getName()));

    // Find saga command reply handler method
    Method sagaCommandReplyHandlerMethod = sagaOrchestratorMetadata.getSagaCommandReplyHandlerMethods()
        .stream()
        .filter(entry -> entry.getKey().name().equals(sagaCommandEntity.getName()))
        .findFirst()
        .orElseThrow(() -> new SagaConfigException(String.format(
            "No command reply handler found for command %s in saga %s", sagaCommandEntity.getName(),
            sagaEntity.getName())));

    try {
      sagaCommandReplyHandlerMethod.invoke(sagaOrchestratorMetadata.getOrchestratorInstance());
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new SagaExecutionException(String.format(
          "Failed to execute command reply handler: saga={id=%d, name=%s}, command={id=%d, name=%s}",
          sagaEntity.getId(), sagaEntity.getName(), sagaCommandEntity.getId(),
          sagaCommandEntity.getName()), e);
    }
  }

  // Compensation result handlers
  // -----------------------------------------------------------------------------------------------

  @Transactional
  public void onCompensationSucceeded(StepCompensationReply reply) {
    SagaCommandEntity stepEntity = sagaCommandRepository.findById(reply.getStepId())
        .orElseThrow(
            () -> new SagaStepNotFoundException("Saga step not found: " + reply.getStepId()));

    // Mark step as COMPENSATED
    stepEntity.setStepStatus(SagaCommandStatus.COMPENSATED);
    sagaCommandRepository.update(stepEntity);
  }

  @Transactional
  public void onCompensationFailed(StepCompensationReply reply) {
    SagaCommandEntity stepEntity = sagaCommandRepository.findById(reply.getStepId())
        .orElseThrow(
            () -> new SagaStepNotFoundException("Saga step not found: " + reply.getStepId()));

    // Mark step as COMPENSATION_FAILED
    stepEntity.setStepStatus(SagaCommandStatus.COMPENSATION_FAILED);
    stepEntity.setCompensationErrorMessage(reply.getErrorMessage());
    stepEntity.setCompletedAt(DateUtil.now());
    sagaCommandRepository.update(stepEntity);
  }
}
