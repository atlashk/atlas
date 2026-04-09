package org.atlas.services.catalog.port.in.chatbot.model;

import java.time.LocalDateTime;
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
public class SendMessageOutput {

  private String conversationId;

  private String text;

  private LocalDateTime createdAt;
}
