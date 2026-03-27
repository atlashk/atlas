package org.atlas.services.catalog.port.in.chatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.catalog.domain.entity.chatbot.MessageType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ChatSendMessageInput {

  private String conversationId;

  private MessageType messageType;

  private String text;
}
