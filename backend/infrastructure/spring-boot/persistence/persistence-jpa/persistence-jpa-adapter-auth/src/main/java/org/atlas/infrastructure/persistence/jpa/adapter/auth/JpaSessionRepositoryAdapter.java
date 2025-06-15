package org.atlas.infrastructure.persistence.jpa.adapter.auth;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.SessionEntity;
import org.atlas.domain.auth.repository.SessionRepository;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.persistence.jpa.adapter.auth.entity.JpaSessionEntity;
import org.atlas.infrastructure.persistence.jpa.adapter.auth.repository.JpaSessionRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaSessionRepositoryAdapter implements SessionRepository {

  private final JpaSessionRepository jpaSessionRepository;

  @Override
  public Optional<SessionEntity> findById(String id) {
    return jpaSessionRepository.findById(id)
        .map(jpaSessionEntity -> ObjectMapperUtil.getInstance()
            .map(jpaSessionEntity, SessionEntity.class));
  }

  @Override
  public void insert(SessionEntity sessionEntity) {
    JpaSessionEntity jpaSessionEntity = ObjectMapperUtil.getInstance()
        .map(sessionEntity, JpaSessionEntity.class);
    jpaSessionRepository.save(jpaSessionEntity);
  }

  @Override
  public void update(SessionEntity sessionEntity) {
    JpaSessionEntity jpaSessionEntity = ObjectMapperUtil.getInstance()
        .map(sessionEntity, JpaSessionEntity.class);
    jpaSessionRepository.save(jpaSessionEntity);
  }

  @Override
  public void deleteById(String id) {
    jpaSessionRepository.deleteById(id);
  }
}
