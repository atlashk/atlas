package org.atlas.infrastructure.transaction;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.transaction.TransactionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
@RequiredArgsConstructor
public class TransactionAdapter implements TransactionPort {

  private final PlatformTransactionManager transactionManager;

  private final ThreadLocal<TransactionStatus> currentTransaction = new ThreadLocal<>();

  @Override
  public void begin() {
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus status = transactionManager.getTransaction(def);
    currentTransaction.set(status);
  }

  @Override
  public void commit() {
    TransactionStatus status = currentTransaction.get();
    if (status != null && !status.isCompleted()) {
      transactionManager.commit(status);
      currentTransaction.remove();
    }
  }

  @Override
  public void rollback() {
    TransactionStatus status = currentTransaction.get();
    if (status != null && !status.isCompleted()) {
      transactionManager.rollback(status);
      currentTransaction.remove();
    }
  }

  @Override
  public void execute(Runnable runnable) {
    begin();
    try {
      runnable.run();
      commit();
    } catch (Exception e) {
      rollback();
      throw e;
    }
  }

  @Override
  public <T> T execute(Supplier<T> supplier) {
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
