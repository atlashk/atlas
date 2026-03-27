package org.atlas.services.catalog.port.in.chatbot.service;

import java.util.List;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.services.catalog.domain.entity.chatbot.ChatMessageEntity;
import org.atlas.services.catalog.port.in.chatbot.model.ChatSendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;

public interface ChatMessageService {

  List<ChatMessageEntity> retrieveMessageList(String conversationId, PagingRequest pagingRequest);

  SendMessageOutput sendMessage(ChatSendMessageInput chatSendMessageInput);

  void deleteAllMessages(String conversationId);
}
