package org.atlas.services.iam.infrastructure.persistence.jpa.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.infrastructure.persistence.jpa.entity.JpaUserEntity;
import org.atlas.services.iam.infrastructure.persistence.jpa.mapper.JpaUserMapper;
import org.atlas.services.iam.infrastructure.persistence.jpa.repository.JpaUserRepository;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public PagingResult<UserEntity> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest) {
    long totalCount = countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }

    List<JpaUserEntity> jpaUsers = findJpaUsersByCriteria(criteria, pagingRequest);
    List<UserEntity> users = MapperUtil.mapList(jpaUsers, JpaUserMapper.INSTANCE::toUser);
    return PagingResult.of(users, totalCount, pagingRequest);
  }

  @Override
  public List<UserEntity> findByUserIdIn(List<String> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return List.of();
    }
    List<JpaUserEntity> jpaUsers = jpaUserRepository.findAllById(userIds);
    return MapperUtil.mapList(jpaUsers, JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<UserEntity> findByUserId(String userId) {
    return jpaUserRepository.findById(userId)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<UserEntity> findByUsername(String username) {
    return jpaUserRepository.findByUsername(username)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<UserEntity> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<UserEntity> findByPhoneNumber(String phoneNumber) {
    return jpaUserRepository.findByPhoneNumber(phoneNumber)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Long countAll() {
    return jpaUserRepository.count();
  }

  @Override
  public void insert(UserEntity user) {
    JpaUserEntity jpaUser = JpaUserMapper.INSTANCE.toJpaUser(user);
    jpaUserRepository.save(jpaUser);
    user.setUserId(jpaUser.getUserId());
  }

  @Override
  public void update(UserEntity user) {
    JpaUserEntity jpaUser = JpaUserMapper.INSTANCE.toJpaUser(user);
    jpaUserRepository.save(jpaUser);
  }

  @Override
  public void deleteByUserId(String userId) {
    jpaUserRepository.deleteById(userId);
  }

  private List<JpaUserEntity> findJpaUsersByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest) {
    PagingRequest effectivePaging = pagingRequest == null ? PagingRequest.unpaged() : pagingRequest;

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<JpaUserEntity> criteriaQuery = criteriaBuilder.createQuery(JpaUserEntity.class);
    Root<JpaUserEntity> root = criteriaQuery.from(JpaUserEntity.class);

    Predicate predicate = buildPredicate(criteria, criteriaBuilder, root);
    criteriaQuery.select(root).where(predicate);

    applySort(criteriaQuery, criteriaBuilder, root, effectivePaging);

    TypedQuery<JpaUserEntity> query = entityManager.createQuery(criteriaQuery);
    applyPaging(query, effectivePaging);
    return query.getResultList();
  }

  private long countByCriteria(FindUserCriteria criteria) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    Root<JpaUserEntity> root = criteriaQuery.from(JpaUserEntity.class);

    Predicate predicate = buildPredicate(criteria, criteriaBuilder, root);
    criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);

    Long result = entityManager.createQuery(criteriaQuery).getSingleResult();
    return result == null ? 0L : result;
  }

  private Predicate buildPredicate(FindUserCriteria criteria, CriteriaBuilder criteriaBuilder,
      Root<JpaUserEntity> root) {
    if (criteria == null) {
      return criteriaBuilder.conjunction();
    }

    List<Predicate> predicates = new ArrayList<>();

    if (StringUtil.isNotBlank(criteria.getUserId())) {
      predicates.add(criteriaBuilder.equal(root.get("userId"), criteria.getUserId()));
    }

    if (criteria.getRole() != null) {
      predicates.add(criteriaBuilder.equal(root.get("role"), criteria.getRole()));
    }

    if (StringUtil.isNotBlank(criteria.getUsername())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")),
          "%" + criteria.getUsername().trim().toLowerCase() + "%"));
    }

    if (StringUtil.isNotBlank(criteria.getFirstName())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),
          "%" + criteria.getFirstName().trim().toLowerCase() + "%"));
    }

    if (StringUtil.isNotBlank(criteria.getLastName())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")),
          "%" + criteria.getLastName().trim().toLowerCase() + "%"));
    }

    if (StringUtil.isNotBlank(criteria.getEmail())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
          "%" + criteria.getEmail().trim().toLowerCase() + "%"));
    }

    if (StringUtil.isNotBlank(criteria.getPhoneNumber())) {
      predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")),
          "%" + criteria.getPhoneNumber().trim().toLowerCase() + "%"));
    }

    if (predicates.isEmpty()) {
      return criteriaBuilder.conjunction();
    }
    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }

  private void applySort(CriteriaQuery<JpaUserEntity> criteriaQuery, CriteriaBuilder criteriaBuilder,
      Root<JpaUserEntity> root, PagingRequest pagingRequest) {
    if (!pagingRequest.hasSort()) {
      return;
    }
    String sortBy = normalizeSortBy(pagingRequest.getSortBy());
    if (pagingRequest.isSortAscending()) {
      criteriaQuery.orderBy(criteriaBuilder.asc(root.get(sortBy)));
    } else {
      criteriaQuery.orderBy(criteriaBuilder.desc(root.get(sortBy)));
    }
  }

  private void applyPaging(TypedQuery<?> query, PagingRequest pagingRequest) {
    if (!pagingRequest.hasPaging()) {
      return;
    }
    query.setFirstResult(pagingRequest.getOffset());
    query.setMaxResults(pagingRequest.getLimit());
  }

  private String normalizeSortBy(String sortBy) {
    if (StringUtil.isBlank(sortBy)) {
      return sortBy;
    }
    return "id".equals(sortBy) ? "userId" : sortBy;
  }
}
