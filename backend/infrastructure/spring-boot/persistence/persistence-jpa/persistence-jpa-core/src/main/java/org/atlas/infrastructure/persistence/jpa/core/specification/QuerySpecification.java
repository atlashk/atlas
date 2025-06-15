package org.atlas.infrastructure.persistence.jpa.core.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class QuerySpecification<T> implements Specification<T> {

  private final List<QueryFilter> filters = new ArrayList<>();

  public void addFilter(QueryFilter filter) {
    filters.add(filter);
  }

  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder) {
    List<Predicate> predicates = new ArrayList<>();
    for (QueryFilter filter : filters) {
      predicates.add(createPredicateForFilter(filter, root, criteriaBuilder));
    }
    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }

  private Predicate createPredicateForFilter(QueryFilter filter, Root<T> root,
      CriteriaBuilder criteriaBuilder) {
    List<Predicate> predicates = new ArrayList<>();
    for (QueryFilter.Condition condition : filter.getConditions()) {
      predicates.add(createSimplePredicate(condition, root, criteriaBuilder));
    }

    if (filter.getConditions().size() == 1) {
      return predicates.get(0);
    } else {
      if (filter.getLogicalOperator() == LogicalOperator.OR) {
        return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
      } else {
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Predicate createSimplePredicate(QueryFilter.Condition condition, Root<T> root,
      CriteriaBuilder criteriaBuilder) {
    Path path = getPath(condition, root);
    return switch (condition.getQueryOperator()) {
      case GREATER_THAN -> criteriaBuilder.greaterThan(path, (Comparable) condition.getValue());
      case LESS_THAN -> criteriaBuilder.lessThan(path, (Comparable) condition.getValue());
      case GREATER_THAN_EQUAL ->
          criteriaBuilder.greaterThanOrEqualTo(path, (Comparable) condition.getValue());
      case LESS_THAN_EQUAL ->
          criteriaBuilder.lessThanOrEqualTo(path, (Comparable) condition.getValue());
      case EQUAL -> criteriaBuilder.equal(path, condition.getValue());
      case NOT_EQUAL -> criteriaBuilder.notEqual(path, condition.getValue());
      case LIKE -> criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)),
          "%" + condition.getValue().toString().toLowerCase() + "%");
      case LIKE_START -> criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)),
          "%" + condition.getValue().toString().toLowerCase());
      case LIKE_END -> criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)),
          condition.getValue().toString().toLowerCase() + "%");
      case IN -> path.in((Collection<?>) condition.getValue());
      case NOT_IN -> criteriaBuilder.not(path.in((Collection<?>) condition.getValue()));
      default -> throw new IllegalArgumentException(
          "Unsupported operation: " + condition.getQueryOperator());
    };
  }

  private <X, Y> Path<Y> getPath(QueryFilter.Condition condition, Root<X> root) {
    if (condition.getKey().contains(".")) {
      String[] tokens = condition.getKey().split("\\.");
      return root.join(tokens[0]).get(tokens[1]);
    } else {
      return root.get(condition.getKey());
    }
  }
}
