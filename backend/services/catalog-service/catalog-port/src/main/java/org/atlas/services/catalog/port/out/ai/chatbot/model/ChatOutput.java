package org.atlas.services.catalog.port.out.ai.chatbot.model;

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
public class ChatOutput {

  private String message;

  private Integer inputTokens;

  private Integer outputTokens;

  public ChatOutput(String message) {
    this.message = message;
  }
}
