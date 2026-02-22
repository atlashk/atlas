package org.atlas.services.identity.port.out.repository;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.identity.UserRole;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.identity.domain.entity.UserEntity;

public interface UserRepository {

  PagingResult<UserEntity> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  List<UserEntity> findByIdIn(List<String> ids);

  Optional<UserEntity> findById(String id);

  Optional<UserEntity> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByPhoneNumber(String phoneNumber);

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

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private UserRole role;
  }
}
