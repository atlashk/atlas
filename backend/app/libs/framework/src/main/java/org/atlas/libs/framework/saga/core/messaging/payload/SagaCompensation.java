package org.atlas.libs.framework.saga.core.messaging.payload;

import java.io.Serializable;
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
public class SagaCompensation implements Serializable {

  private Integer sagaId;
  private String sagaName;
  private String sagaCommandName;
  private String targetServiceName;
  private String sagaContext;
}