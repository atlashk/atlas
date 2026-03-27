package org.atlas.services.catalog.application.chatbot.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.error.DomainError;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.framework.uuid.UUIDGenerator;
import org.atlas.services.catalog.domain.entity.chatbot.ChatMessageEntity;
import org.atlas.services.catalog.domain.entity.chatbot.SenderType;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;
import org.atlas.services.catalog.port.in.chatbot.service.MessageService;
import org.atlas.services.catalog.port.out.ai.chatbot.model.ChatInput;
import org.atlas.services.catalog.port.out.ai.chatbot.model.ChatOutput;
import org.atlas.services.catalog.port.out.ai.chatbot.service.RagService;
import org.atlas.services.catalog.port.out.repository.chatbot.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final RagService ragService;

  /**
   * Refer <a
   * href="https://www.promptingguide.ai/introduction/examples.en#question-answering">Question
   * Answering prompt technique</a>.
   */
  private static final String PROMPT_TEMPLATE = """
      You are a helpful assistant. Use the following information to answer the question in detail. Please use a friendly and professional tone. Please acknowledge the question and relate the answer back to it.\s
      If the answer is not in the provided information, say "I don't know."
      
      Information:
      {context}
      
      Answer:
      """;
  private static final int TOP_K = 3;
  private static final double SIMILARITY_THRESHOLD = 0.3;
  private static final String ASSISTANT_USER_ID = "assistant";

  @Override
  @Transactional(readOnly = true)
  public List<ChatMessageEntity> retrieveMessageList(String conversationId,
      PagingRequest pagingRequest) {
    return messageRepository.findByConversationId(conversationId, pagingRequest);
  }

  @Override
  @Transactional
  public SendMessageOutput sendMessage(SendMessageInput sendMessageInput) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();

    if (StringUtil.isBlank(sendMessageInput.getConversationId())) {
      throw new DomainException(CommonDomainError.BAD_REQUEST, "conversationId must not be blank");
    }

    // Save input message
    ChatMessageEntity inputMessage = ChatMessageEntity.builder()
        .id(UUIDGenerator.generate())
        .conversationId(sendMessageInput.getConversationId())
        .messageType(sendMessageInput.getMessageType())
        .senderType(SenderType.USER)
        .userId(userId)
        .text(sendMessageInput.getText())
        .build();
    messageRepository.insert(inputMessage);

    // Chat via RAG
    ChatInput chatInput = ChatInput.builder()
        .conversationId(sendMessageInput.getConversationId())
        .userMessage(sendMessageInput.getText())
        .promptTemplate(PROMPT_TEMPLATE)
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .build();

    log.debug("Start sending message: conversationId={}, userId={}",
      sendMessageInput.getConversationId(), userId);
    ChatOutput chatOutput = ragService.chat(chatInput);
    log.debug("End sending message: conversationId={}, userId={}, inputTokens={}, outputTokens={}",
      sendMessageInput.getConversationId(), userId, chatOutput.getInputTokens(), chatOutput.getOutputTokens());

    // Save output message
    ChatMessageEntity outputMessage = ChatMessageEntity.builder()
        .id(UUIDGenerator.generate())
        .conversationId(sendMessageInput.getConversationId())
        .messageType(sendMessageInput.getMessageType())
        .senderType(SenderType.ASSISTANT)
        .userId(ASSISTANT_USER_ID)
        .text(chatOutput.getMessage())
        .build();
    messageRepository.insert(outputMessage);

    return SendMessageOutput.builder()
        .text(outputMessage.getText())
        .createdAt(LocalDateTime.now())
        .build();
  }

  @Override
  @Transactional
  public void deleteAllMessages(String conversationId) {
    messageRepository.deleteByConversationId(conversationId);
  }
}
