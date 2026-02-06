package org.atlas.services.iam.port.out.repository;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.iam.domain.entity.UserEntity;

public interface UserRepository {

  PagingResult<UserEntity> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  List<UserEntity> findByUserIdIn(List<String> userIds);

  Optional<UserEntity> findByUserId(String userId);

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByPhoneNumber(String phoneNumber);

  Long countAll();

  void insert(UserEntity user);

  void update(UserEntity user);

  void deleteByUserId(String userId);

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class FindUserCriteria {

    private String userId;

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private UserRole role;
  }
}
