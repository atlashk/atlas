package org.atlas.framework.saga.core.messaging.payload;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.saga.core.compensation.SagaCompensationResult;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SagaCompensationReply implements Serializable {

  private Integer sagaId;
  private String sagaName;
  private String sagaCommandName;
  private SagaCompensationResult result;
}
