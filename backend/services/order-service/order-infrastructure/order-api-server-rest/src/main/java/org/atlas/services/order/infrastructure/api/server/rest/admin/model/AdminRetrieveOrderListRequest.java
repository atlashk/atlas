package org.atlas.services.order.infrastructure.api.server.rest.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.order.OrderStatus;

@Schema(description = "Request object for retrieving admin order list")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminRetrieveOrderListRequest {

  @Schema(description = "Order ID", example = "1")
  private String id;

  @Schema(description = "User ID", example = "1")
  private String userId;

  @Schema(description = "Product ID", example = "1")
  private String productId;

  @Schema(description = "Order status")
  private OrderStatus status;

  @Schema(description = "Start date", example = "2024-01-01")
  private Date startDate;

  @Schema(description = "End date", example = "2024-01-31")
  private Date endDate;

  @Positive
  @Min(1)
  @Schema(description = "The page number", example = "1", defaultValue = "1")
  @Builder.Default
  private int page = 1;

  @Positive
  @Min(0)
  @Schema(description = "The number of records per page", example = "20", defaultValue = "20")
  @Builder.Default
  private int size = CommonConstant.DEFAULT_PAGE_SIZE;
}
