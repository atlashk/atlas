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
public class ChatInput {

  private String conversationId;

  private String userMessage;

  private String promptTemplate;

  private Integer topK;

  private Double similarityThreshold;
}
