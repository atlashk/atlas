package org.atlas.framework.transaction;

import java.util.function.Supplier;

public interface TransactionPort {

  void begin();

  void commit();

  void rollback();

  default void execute(Runnable runnable) {
    begin();
    try {
      runnable.run();
      commit();
    } catch (Exception e) {
      rollback();
      throw e;
    }
  }

  default <T> T execute(Supplier<T> supplier) {
    begin();
    try {
      T result = supplier.get();
      commit();
      return result;
    } catch (Exception e) {
      rollback();
      throw e;
    }
  }
}
