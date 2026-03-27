package org.atlas.services.catalog.infrastructure.ai.chatbot.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.NullUtil;
import org.atlas.services.catalog.port.out.ai.chatbot.model.ChatInput;
import org.atlas.services.catalog.port.out.ai.chatbot.model.ChatOutput;
import org.atlas.services.catalog.port.out.ai.chatbot.service.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagServiceImpl implements RagService {

  private final ChatClient chatClient;
  private final VectorStore vectorStore;

  @Override
  public ChatOutput chat(ChatInput input) {
    // Retrieve similar documents
    SearchRequest searchRequest = SearchRequest.builder()
        .query(input.getUserMessage())
        .topK(NullUtil.nvl(input.getTopK(), 5))
        .similarityThreshold(NullUtil.nvl(input.getSimilarityThreshold(), 0.6))
        .build();
    List<Document> retrievedDocs = vectorStore.similaritySearch(searchRequest);
    log.info("Retrieved {} documents", retrievedDocs.size());
    if (CollectionUtil.isEmpty(retrievedDocs)) {
      return new ChatOutput("Not found matches products");
    }
    // Debug
    for (
        Document retrievedDoc : retrievedDocs) {
      log.info("Retrieved document: {}", retrievedDoc.getMetadata().get("productId"));
    }

    String context = retrievedDocs.stream()
        .map(Document::getText)
        .collect(Collectors.joining("\n"));

    // Augment the prompt
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(input.getPromptTemplate());
    Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", context));
    Message userMessage = new UserMessage(input.getUserMessage());
    Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

    // Generate the response
    ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt(prompt)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, input.getConversationId()))
        .call();

    // Obtain input & output tokens
    ChatResponse chatResponse = callResponseSpec.chatResponse();
    String message = getMessage(chatResponse);
    Integer inputTokens = null;
    Integer outputTokens = null;
    if (chatResponse != null) {
      inputTokens = toInteger(chatResponse.getMetadata().getUsage().getPromptTokens());
      outputTokens = toInteger(chatResponse.getMetadata().getUsage().getCompletionTokens());
    }

    return ChatOutput.builder()
        .message(message)
        .inputTokens(inputTokens)
        .outputTokens(outputTokens)
        .build();
  }

  private String getMessage(ChatResponse chatResponse) {
    if (chatResponse == null || chatResponse.getResult() == null) {
      return null;
    }
    return chatResponse.getResult().getOutput().getText();
  }

  private Integer toInteger(Number value) {
    return value == null ? null : value.intValue();
  }
}
