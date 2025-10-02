package org.atlas.framework.saga.orchestrator;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.saga.repository.SagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SagaDataService {

  private final SagaRepository sagaRepository;

  public Map<String, Object> getSagaData(Long sagaId) {
    return sagaRepository.findSagaDataById(sagaId);
  }
}
