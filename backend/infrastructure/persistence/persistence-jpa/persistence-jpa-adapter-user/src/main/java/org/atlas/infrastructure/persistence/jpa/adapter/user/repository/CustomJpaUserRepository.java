package org.atlas.infrastructure.persistence.jpa.adapter.user.repository;

import java.util.List;
import org.atlas.domain.user.repository.criteria.FindUserCriteria;
import org.atlas.framework.paging.PagingRequest;
import org.atlas.infrastructure.persistence.jpa.adapter.user.entity.JpaUserEntity;

public interface CustomJpaUserRepository {

  List<JpaUserEntity> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest);

  long countByCriteria(FindUserCriteria criteria);
