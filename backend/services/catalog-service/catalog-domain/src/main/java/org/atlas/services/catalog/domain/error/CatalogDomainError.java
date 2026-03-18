package org.atlas.services.catalog.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.error.DomainError;

@Getter
@RequiredArgsConstructor
public enum CatalogDomainError implements DomainError {

  // Product-related errors
  PRODUCT_NOT_FOUND(2200, "error.product.not_found"),
  NO_IMPORTED_PRODUCT(2201, "error.product.no_imported_product"),
  FAILED_TO_IMPORT_PRODUCT(2203, "error.product.failed_to_import_product"),
  ;

  private final int errorCode;
  private final String messageCode;
}
