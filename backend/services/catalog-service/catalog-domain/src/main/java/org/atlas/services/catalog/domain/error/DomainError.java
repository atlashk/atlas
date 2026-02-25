package org.atlas.services.catalog.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainError {

  // Brand-related error
  BRAND_NOT_FOUND(2000, "error.product.brand.not_found"),

  // Category-related error
  CATEGORY_NOT_FOUND(2100, "error.product.category.not_found"),

  // Product-related errors
  PRODUCT_NOT_FOUND(2200, "error.product.not_found"),
  NO_IMPORTED_PRODUCT(2201, "error.product.no_imported_product"),
  FAILED_TO_IMPORT_PRODUCT(2203, "error.product.failed_to_import_product"),
  ;

  private final int errorCode;
  private final String messageCode;

  @Override
  public String toString() {
    return String.format("%d %s", errorCode, name());
  }
}
