package org.atlas.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

public interface UserRepository {

  PagingResult<UserEntity> findAll(PagingRequest pagingRequest);

  List<UserEntity> findByIdIn(List<Integer> ids);

  Optional<UserEntity> findById(Integer id);

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByPhoneNumber(String phoneNumber);

  void insert(UserEntity userEntity);
}
