package org.atlas.domain.order.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.order.repository.OrderRepository;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class AdminCountOrderUseCaseHandler {

  private final OrderRepository orderRepository;

  public Long handle() throws Exception {
    return orderRepository.countAll();
  }
}
