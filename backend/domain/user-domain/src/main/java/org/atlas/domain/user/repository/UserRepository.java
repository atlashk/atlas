package org.atlas.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.framework.paging.PagingResult;

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
