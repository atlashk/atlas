package org.atlas.services.catalog.api.rest.chatbot.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.chatbot.MessageType;

@Schema(description = "Request object for sending a message")
@Getter
@Setter
public class SendChatMessageRequest {

  @Schema(description = "Conversation ID", example = "01HV4Y7G2D2Q17W0P1D3YH7G8N")
  private String conversationId;

  @NotNull
  @Schema(description = "Message type", example = "TEXT")
  private MessageType messageType;

  @NotBlank
  @Schema(description = "Message text", example = "Recommend me a budget phone.")
  private String text;
}
