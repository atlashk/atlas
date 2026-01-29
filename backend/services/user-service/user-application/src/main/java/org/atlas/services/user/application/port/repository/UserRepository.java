package org.atlas.services.user.application.port.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.services.user.application.port.repository.criteria.FindUserCriteria;
import org.atlas.services.user.domain.entity.User;

public interface UserRepository {

  PagingResult<User> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  List<User> findByIdIn(List<Integer> ids);

  Optional<User> findById(Integer id);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findByPhoneNumber(String phoneNumber);

  Long countAll();

  void insert(User user);
}
