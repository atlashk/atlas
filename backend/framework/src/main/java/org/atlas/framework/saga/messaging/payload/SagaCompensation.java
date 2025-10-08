package org.atlas.framework.saga.messaging.payload;

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
public class SagaCompensation {

  private Long sagaId;
  private String sagaName;
  private String sagaCommandName;
  private String targetServiceName;
  private String sagaContext;

  @Override
  public String toString() {
    return "{" +
        "sagaId=" + sagaId +
        ", sagaName='" + sagaName + '\'' +
        ", sagaCommandName='" + sagaCommandName + '\'' +
        ", targetServiceName='" + targetServiceName + '\'' +
        '}';
  }
}