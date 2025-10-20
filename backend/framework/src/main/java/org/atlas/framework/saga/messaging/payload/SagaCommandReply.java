package org.atlas.framework.saga.messaging.payload;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.saga.command.SagaCommandResult;

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
