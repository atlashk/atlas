package org.atlas.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

public interface UserRepository {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findByPhoneNumber(String phoneNumber);

  void insert(User user);
}
