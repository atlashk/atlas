package org.atlas.services.iam.port.out.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.out.repository.criteria.FindUserCriteria;

public interface UserRepository {

  PagingResult<UserEntity> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  List<UserEntity> findByIdIn(List<String> ids);

  Optional<UserEntity> findById(String id);

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByPhoneNumber(String phoneNumber);

  Long countAll();

  void insert(UserEntity user);

  void update(UserEntity user);

  void deleteById(String id);
}
