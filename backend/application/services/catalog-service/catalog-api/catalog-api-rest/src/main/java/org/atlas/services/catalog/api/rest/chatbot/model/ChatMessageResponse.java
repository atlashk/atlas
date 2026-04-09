package org.atlas.services.catalog.api.rest.chatbot.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.chatbot.MessageType;
import org.atlas.services.catalog.domain.entity.chatbot.SenderType;

@Schema(description = "Response object for a chatbot message")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ChatMessageResponse {

  @Schema(description = "Unique identifier of the message", example = "01HV4Y8D4NZQFW5QYVZ53D8J9K")
  private String id;

  @Schema(description = "Conversation ID", example = "01HV4Y7G2D2Q17W0P1D3YH7G8N")
  private String conversationId;

  @Schema(description = "Message type", example = "TEXT")
  private MessageType messageType;

  @Schema(description = "Sender type", example = "ASSISTANT")
  private SenderType senderType;

  @Schema(description = "Message content", example = "You can return a product within 30 days.")
  private String text;

  @Schema(description = "Message creation time", example = "2026-03-26T10:20:05")
  private LocalDateTime createdAt;
}
