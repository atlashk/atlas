package org.atlas.services.order.port.in.order.model.admin;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.order.OrderStatus;
import org.atlas.libs.framework.paging.PagingRequest;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveOrderListInput {

  private String id;

  private String userId;

  private String productId;

  private OrderStatus status;

  private LocalDate startDate;

  private LocalDate endDate;

  private PagingRequest pagingRequest;
}
