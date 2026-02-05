package org.atlas.services.notification.domain.entity.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class OrderFulfilledMetadata {

  private String orderId;
}
