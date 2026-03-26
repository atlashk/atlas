package org.atlas.services.catalog.application.chatbot.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.framework.uuid.UUIDGenerator;
import org.atlas.services.catalog.domain.entity.chatbot.ConversationEntity;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;
import org.atlas.services.catalog.port.in.chatbot.service.ConversationService;
import org.atlas.services.catalog.port.in.chatbot.service.MessageService;
import org.atlas.services.catalog.port.out.repository.chatbot.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

  private final ConversationRepository conversationRepository;
  private final MessageService messageService;

  private static final int TITLE_MAX_LENGTH = 50;

  @Override
  @Transactional(readOnly = true)
  public List<ConversationEntity> retrieveConversationList(PagingRequest pagingRequest) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
    return conversationRepository.findByUserId(userId, pagingRequest);
  }

  @Override
  @Transactional
  public SendMessageOutput startConversation(SendMessageInput input) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();

    // Create new conversation
    ConversationEntity conversation = ConversationEntity.builder()
        .id(UUIDGenerator.generate())
        .userId(userId)
        .title(StringUtil.limitLength(input.getText(), TITLE_MAX_LENGTH))
        .build();
    conversationRepository.insert(conversation);

    // Send first message
    SendMessageInput sendMessageInput = SendMessageInput.builder()
        .conversationId(conversation.getId())
        .text(input.getText())
        .build();
    SendMessageOutput sendMessageOutput = messageService.sendMessage(sendMessageInput);

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
    messageService.deleteAllMessages(conversationId);
  }
}
