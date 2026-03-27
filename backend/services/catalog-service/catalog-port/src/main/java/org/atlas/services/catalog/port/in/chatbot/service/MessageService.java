package org.atlas.services.catalog.port.in.chatbot.service;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.ChatMessageEntity;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;

public interface MessageService {

  List<ChatMessageEntity> retrieveMessageList(String conversationId, PagingRequest pagingRequest);

  SendMessageOutput sendMessage(SendMessageInput sendMessageInput);

  void deleteAllMessages(String conversationId);
}
