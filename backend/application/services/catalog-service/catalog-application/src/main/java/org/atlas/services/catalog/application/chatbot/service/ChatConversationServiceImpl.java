package org.atlas.services.catalog.application.chatbot.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.framework.uuid.UUIDGenerator;
import org.atlas.services.catalog.domain.entity.chatbot.ChatConversationEntity;
import org.atlas.services.catalog.port.in.chatbot.model.ChatSendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;
import org.atlas.services.catalog.port.in.chatbot.service.ChatConversationService;
import org.atlas.services.catalog.port.in.chatbot.service.ChatMessageService;
import org.atlas.services.catalog.port.out.repository.chatbot.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatConversationServiceImpl implements ChatConversationService {

  private final ConversationRepository conversationRepository;
  private final ChatMessageService chatMessageService;

  private static final int TITLE_MAX_LENGTH = 50;

  @Override
  @Transactional(readOnly = true)
  public List<ChatConversationEntity> retrieveConversationList(PagingRequest pagingRequest) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
    return conversationRepository.findByUserId(userId, pagingRequest);
  }

  @Override
  @Transactional
  public SendMessageOutput startConversation(ChatSendMessageInput startConversationInput) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();

    // Create new conversation
    ChatConversationEntity conversation = ChatConversationEntity.builder()
        .id(UUIDGenerator.generate())
        .userId(userId)
        .title(StringUtil.limitLength(startConversationInput.getText(), TITLE_MAX_LENGTH))
        .build();
    conversationRepository.insert(conversation);

    // Send first message
    ChatSendMessageInput sendMessageInput = ChatSendMessageInput.builder()
        .conversationId(conversation.getId())
        .messageType(startConversationInput.getMessageType())
        .text(startConversationInput.getText())
        .build();
    SendMessageOutput sendMessageOutput = chatMessageService.sendMessage(sendMessageInput);

    return SendMessageOutput.builder()
        .conversationId(conversation.getId())
        .text(sendMessageOutput.getText())
        .createdAt(sendMessageOutput.getCreatedAt())
        .build();
  }

  @Override
  @Transactional
  public void deleteConversation(String conversationId) {
    // Delete conversation
    conversationRepository.delete(conversationId);

    // Delete messages of conversation
    chatMessageService.deleteAllMessages(conversationId);
  }
}
