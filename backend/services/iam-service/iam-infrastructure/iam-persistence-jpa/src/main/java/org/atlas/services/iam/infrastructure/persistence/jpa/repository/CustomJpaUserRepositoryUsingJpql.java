package org.atlas.services.iam.infrastructure.persistence.jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.iam.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class CustomJpaUserRepositoryUsingJpql implements CustomJpaUserRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<JpaUserEntity> findByCriteria(UserRepository.FindUserCriteria criteria,
      PagingRequest pagingRequest) {
    StringBuilder sqlBuilder = new StringBuilder("""
        select u
        from JpaUserEntity u
        """);

    Map<String, Object> params = new HashMap<>();
    sqlBuilder.append(buildWhereClause(criteria, params));

    if (pagingRequest.hasSort()) {
      sqlBuilder.append(" order by u.").append(pagingRequest.getSortBy());
      if (pagingRequest.isSortDescending()) {
        sqlBuilder.append(" desc");
      }
    }

    String sql = sqlBuilder.toString();
    TypedQuery<JpaUserEntity> query = entityManager.createQuery(sql, JpaUserEntity.class);

    params.forEach(query::setParameter);

    if (pagingRequest.hasPaging()) {
      query.setFirstResult(pagingRequest.getOffset());
      query.setMaxResults(pagingRequest.getLimit());
    }

    return query.getResultList();
  }

  @Override
  public long countByCriteria(UserRepository.FindUserCriteria criteria) {
    Map<String, Object> params = new HashMap<>();
    String whereClause = buildWhereClause(criteria, params);
    String countSql = """
        select count(distinct u.userId)
        from JpaUserEntity u
        """ + whereClause;
    TypedQuery<Long> countQuery = entityManager.createQuery(countSql, Long.class);
    params.forEach(countQuery::setParameter);
    return countQuery.getSingleResult();
  }

  private String buildWhereClause(UserRepository.FindUserCriteria criteria,
      Map<String, Object> params) {
    StringBuilder whereClauseBuilder = new StringBuilder("where 1=1 ");

    if (StringUtil.isNotBlank(criteria.getUserId())) {
      whereClauseBuilder.append(" and u.userId = :userId ");
      params.put("userId", criteria.getUserId());
    }

    if (StringUtil.isNotBlank(criteria.getUsername())) {
      whereClauseBuilder.append(" and lower(u.username) like :username ");
      params.put("username", "%" + criteria.getUsername().toLowerCase() + "%");
    }

    if (StringUtil.isNotBlank(criteria.getFirstName())) {
      whereClauseBuilder.append(" and lower(u.firstName) like :firstName ");
      params.put("firstName", "%" + criteria.getFirstName().toLowerCase() + "%");
    }

    if (StringUtil.isNotBlank(criteria.getLastName())) {
      whereClauseBuilder.append(" and lower(u.lastName) like :lastName ");
      params.put("lastName", "%" + criteria.getLastName().toLowerCase() + "%");
    }

    if (StringUtil.isNotBlank(criteria.getEmail())) {
      whereClauseBuilder.append(" and lower(u.email) like :email ");
      params.put("email", "%" + criteria.getEmail().toLowerCase() + "%");
    }

    if (StringUtil.isNotBlank(criteria.getPhoneNumber())) {
      whereClauseBuilder.append(" and lower(u.phoneNumber) like :phoneNumber ");
      params.put("phoneNumber", "%" + criteria.getPhoneNumber().toLowerCase() + "%");
    }

    if (criteria.getRole() != null) {
      whereClauseBuilder.append(" and u.role = :role ");
      params.put("role", criteria.getRole());
    }

    return whereClauseBuilder.toString();
  }
}
