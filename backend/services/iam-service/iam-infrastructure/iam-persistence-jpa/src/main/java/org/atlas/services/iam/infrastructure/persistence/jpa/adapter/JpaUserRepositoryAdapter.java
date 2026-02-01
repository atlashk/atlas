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
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.util.ObjectMapperUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.iam.domain.entity.User;
import org.atlas.services.iam.infrastructure.persistence.jpa.entity.JpaUser;
import org.atlas.services.iam.infrastructure.persistence.jpa.mapper.JpaUserMapper;
import org.atlas.services.iam.infrastructure.persistence.jpa.repository.JpaUserRepository;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.atlas.services.iam.port.out.repository.criteria.FindUserCriteria;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaUserRepository;

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public PagingResult<User> findByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest) {
    long totalCount = countByCriteria(criteria);
    if (totalCount == 0L) {
      return PagingResult.empty();
    }

    List<JpaUser> jpaUsers = findJpaUsersByCriteria(criteria, pagingRequest);
    List<User> users = ObjectMapperUtil.mapList(jpaUsers, JpaUserMapper.INSTANCE::toUser);
    return PagingResult.of(users, totalCount, pagingRequest);
  }

  @Override
  public List<User> findByIdIn(List<Integer> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      return List.of();
    }
    List<JpaUser> jpaUsers = jpaUserRepository.findAllById(ids);
    return ObjectMapperUtil.mapList(jpaUsers, JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<User> findById(Integer id) {
    return jpaUserRepository.findById(id)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaUserRepository.findByUsername(username)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Optional<User> findByPhoneNumber(String phoneNumber) {
    return jpaUserRepository.findByPhoneNumber(phoneNumber)
        .map(JpaUserMapper.INSTANCE::toUser);
  }

  @Override
  public Long countAll() {
    return jpaUserRepository.count();
  }

  @Override
  public void insert(User user) {
    JpaUser jpaUser = JpaUserMapper.INSTANCE.toJpaUser(user);
    jpaUserRepository.save(jpaUser);
    user.setUserId(jpaUser.getUserId());
  }

  @Override
  public void update(User user) {
    JpaUser jpaUser = JpaUserMapper.INSTANCE.toJpaUser(user);
    jpaUserRepository.save(jpaUser);
  }

  @Override
  public void deleteById(Integer id) {
    jpaUserRepository.deleteById(id);
  }

  private List<JpaUser> findJpaUsersByCriteria(FindUserCriteria criteria, PagingRequest pagingRequest) {
    PagingRequest effectivePaging = pagingRequest == null ? PagingRequest.unpaged() : pagingRequest;

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<JpaUser> criteriaQuery = criteriaBuilder.createQuery(JpaUser.class);
    Root<JpaUser> root = criteriaQuery.from(JpaUser.class);

    Predicate predicate = buildPredicate(criteria, criteriaBuilder, root);
    criteriaQuery.select(root).where(predicate);

    applySort(criteriaQuery, criteriaBuilder, root, effectivePaging);

    TypedQuery<JpaUser> query = entityManager.createQuery(criteriaQuery);
    applyPaging(query, effectivePaging);
    return query.getResultList();
  }

  private long countByCriteria(FindUserCriteria criteria) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
    Root<JpaUser> root = criteriaQuery.from(JpaUser.class);

    Predicate predicate = buildPredicate(criteria, criteriaBuilder, root);
    criteriaQuery.select(criteriaBuilder.count(root)).where(predicate);

    Long result = entityManager.createQuery(criteriaQuery).getSingleResult();
    return result == null ? 0L : result;
  }

  private Predicate buildPredicate(FindUserCriteria criteria, CriteriaBuilder criteriaBuilder,
      Root<JpaUser> root) {
    if (criteria == null) {
      return criteriaBuilder.conjunction();
    }

    List<Predicate> predicates = new ArrayList<>();

    if (criteria.getId() != null) {
      predicates.add(criteriaBuilder.equal(root.get("userId"), criteria.getId()));
    }

    if (criteria.getRole() != null) {
      predicates.add(criteriaBuilder.equal(root.get("role"), criteria.getRole()));
    }

    if (StringUtil.isNotBlank(criteria.getKeyword())) {
      String keyword = criteria.getKeyword().trim();
      String lowercaseKeyword = "%" + keyword.toLowerCase() + "%";

      List<Predicate> keywordPredicates = new ArrayList<>();
      keywordPredicates.add(
          criteriaBuilder.like(criteriaBuilder.lower(root.<String>get("username")),
              lowercaseKeyword));
      keywordPredicates.add(criteriaBuilder.equal(root.get("email"), keyword));
      keywordPredicates.add(criteriaBuilder.equal(root.get("phoneNumber"), keyword));
      try {
        Integer userId = Integer.valueOf(keyword);
        keywordPredicates.add(criteriaBuilder.equal(root.get("userId"), userId));
      } catch (NumberFormatException ignored) {
      }

      predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
    }

    if (predicates.isEmpty()) {
      return criteriaBuilder.conjunction();
    }
    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }

  private void applySort(CriteriaQuery<JpaUser> criteriaQuery, CriteriaBuilder criteriaBuilder,
      Root<JpaUser> root, PagingRequest pagingRequest) {
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
