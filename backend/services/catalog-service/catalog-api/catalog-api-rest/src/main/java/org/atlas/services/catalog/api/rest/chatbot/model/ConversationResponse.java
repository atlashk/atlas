package org.atlas.services.catalog.api.rest.chatbot.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Response object for a conversation")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ConversationResponse {

  @Schema(description = "Unique identifier of the conversation", example = "01HV4Y7G2D2Q17W0P1D3YH7G8N")
  private String id;

  @Schema(description = "Title of the conversation", example = "How to return my product?")
  private String title;

  @Schema(description = "Conversation creation time", example = "2026-03-26T10:15:30")
  private LocalDateTime createdAt;

  @Schema(description = "Conversation last update time", example = "2026-03-26T10:18:45")
  private LocalDateTime updatedAt;
}
