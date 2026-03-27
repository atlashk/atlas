package org.atlas.services.catalog.api.rest.chatbot.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Response object for chatbot reply")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SendMessageResponse {

  @Schema(description = "Unique identifier of the conversation", example = "01HV4Y7G2D2Q17W0P1D3YH7G8N")
  private String conversationId;

  @Schema(description = "Assistant message content", example = "Here are some options under $500.")
  private String text;

  @Schema(description = "Assistant message creation time", example = "2026-03-26T10:21:30")
  private LocalDateTime createdAt;
}
