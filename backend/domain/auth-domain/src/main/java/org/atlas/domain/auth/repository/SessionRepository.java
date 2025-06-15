package org.atlas.domain.auth.repository;

import java.util.Optional;
import org.atlas.domain.auth.entity.SessionEntity;

public interface SessionRepository {

  Optional<SessionEntity> findById(String id);

  void insert(SessionEntity sessionEntity);

  void update(SessionEntity sessionEntity);

  void deleteById(String id);
}
