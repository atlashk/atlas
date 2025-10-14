package org.atlas.framework.saga.messaging.payload;

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
public class SagaCommand implements Serializable {

  private Integer sagaId;
  private String sagaName;
  private String sagaContext;
  private String sagaCommandName;
  private String targetServiceName;

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
