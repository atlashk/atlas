package org.atlas.services.order.api.rest.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;

@Schema(description = "Request object for retrieving order list")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveOrderListRequest {

  @Schema(description = "Order status")
  private OrderStatus status;

  @Schema(description = "Start date", example = "2024-01-01")
  private Date startDate;

  @Schema(description = "End date", example = "2024-01-31")
  private Date endDate;

  @Schema(description = "The page number to retrieve (default is 1).", example = "1")
  private Integer page;

  @Schema(description = "The number of orders per page (default is defined by the constant).", example = "10")
  private Integer size;
}
