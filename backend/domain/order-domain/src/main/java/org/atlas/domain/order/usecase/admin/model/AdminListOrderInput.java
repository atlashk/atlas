package org.atlas.domain.order.usecase.admin.model;

import jakarta.validation.Valid;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.domain.order.shared.enums.OrderStatus;
import org.atlas.framework.paging.PagingRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminListOrderInput {

  private Integer orderId;

  private Integer userId;

  private OrderStatus status;

  private Date startDate;

  private Date endDate;

  @Valid
  private PagingRequest pagingRequest;
}
