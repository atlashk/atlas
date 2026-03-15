package org.atlas.platform.authorization.port.out.repository;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.platform.authorization.domain.entity.UserEntity;

public interface UserRepository {

  PagingResult<UserEntity> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  List<UserEntity> findByIdIn(List<String> ids);

  Optional<UserEntity> findById(String id);

  Optional<UserEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);

  Long countAll();

  void insert(UserEntity user);

  void update(UserEntity user);

  void deleteById(String id);

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class FindUserCriteria {

    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private UserRole role;
  }
}
