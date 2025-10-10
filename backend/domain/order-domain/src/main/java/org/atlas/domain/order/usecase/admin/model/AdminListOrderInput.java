package org.atlas.domain.order.usecase.admin.model;

import jakarta.validation.Valid;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.order.shared.OrderStatus;
import org.atlas.framework.paging.PagingRequest;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminListOrderInput {

  private Integer orderId;

  private Integer userId;

  private Integer productId;

  private OrderStatus status;

  private Date startDate;

  private Date endDate;

  @Valid
  private PagingRequest pagingRequest;
}
