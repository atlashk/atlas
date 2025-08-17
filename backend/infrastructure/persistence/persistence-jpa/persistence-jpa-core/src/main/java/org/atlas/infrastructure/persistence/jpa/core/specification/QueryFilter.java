package org.atlas.infrastructure.persistence.jpa.core.specification;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryFilter {

  private List<Condition> conditions;
  private LogicalOperator logicalOperator;

  @Getter

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Condition {

    private String key;
    private Object value;
    private QueryOperator queryOperator;

    public static Condition of(@Nonnull String key, @Nonnull Object value,
        @Nonnull QueryOperator queryOperator) {
      return new Condition(key, value, queryOperator);
    }
  }

  public static QueryFilter of(@Nonnull String key, @Nonnull Object value,
      @Nonnull QueryOperator queryOperator) {
    return and(Condition.of(key, value, queryOperator));
  }

  public static QueryFilter and(@Nonnull Condition... conditions) {
    return new QueryFilter(Arrays.asList(conditions), LogicalOperator.AND);
  }

  public static QueryFilter or(@Nonnull Condition... conditions) {
    return new QueryFilter(Arrays.asList(conditions), LogicalOperator.OR);
  }
}
