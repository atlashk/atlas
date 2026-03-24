package org.atlas.services.catalog.port.out.ai.rag.model;

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

  private String question;

  @Builder.Default
  private Integer topK = 5;

  @Builder.Default
  private Double similarityThreshold = 0.8;
}
