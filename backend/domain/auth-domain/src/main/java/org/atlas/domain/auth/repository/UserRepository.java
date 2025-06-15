package org.atlas.domain.auth.repository;

import java.util.Optional;
import org.atlas.domain.auth.entity.UserEntity;

public interface UserRepository {

  Optional<UserEntity> findById(Integer id);

  Optional<UserEntity> findByIdentifier(String identifier);

  void insert(UserEntity userEntity);
}
