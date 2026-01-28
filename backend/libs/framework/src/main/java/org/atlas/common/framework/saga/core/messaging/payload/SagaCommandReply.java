package org.atlas.common.framework.saga.core.messaging.payload;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.saga.core.command.SagaCommandResult;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SagaCommandReply implements Serializable {

  private Integer sagaId;
  private String sagaName;
  private String sagaCommandName;
  private SagaCommandResult sagaCommandResult;
}
