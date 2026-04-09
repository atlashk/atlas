package org.atlas.libs.framework.domain.shared.inventory;

public class InsufficientStockException extends Exception {

  public InsufficientStockException() {
  }

  public InsufficientStockException(Throwable cause) {
    super(cause);
  }
}
