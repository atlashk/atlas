package org.atlas.services.catalog.infrastructure.ai.chatbot.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.catalog.port.out.ai.chatbot.model.SendMessageInput;
import org.atlas.services.catalog.port.out.ai.chatbot.model.SendMessageOutput;
import org.atlas.services.catalog.port.out.ai.chatbot.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

  private final ChatClient chatClient;
  private final VectorStore vectorStore;

  public ChatServiceImpl(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
      VectorStore vectorStore) {
    this.chatClient = chatClientBuilder
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();
    this.vectorStore = vectorStore;
  }

  private static final int topK = 3;
  private static final double similarityThreshold = 0.3;

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

  @Override
  public SendMessageOutput sendMessage(SendMessageInput input) {
    // 1. Retrieve similar documents
    SearchRequest searchRequest = SearchRequest.builder()
        .query(input.getMessage())
        .topK(topK)
        .similarityThreshold(similarityThreshold)
        .build();
    List<Document> retrievedDocs = vectorStore.similaritySearch(searchRequest);
    log.info("Retrieved {} documents", retrievedDocs.size());
    if (CollectionUtil.isEmpty(retrievedDocs)) {
      return new SendMessageOutput("Not found matches products");
    }
    // Debug
    for (
        Document retrievedDoc : retrievedDocs) {
      log.info("Retrieved document: {}", retrievedDoc.getMetadata().get("productId"));
    }

    String context = retrievedDocs.stream()
        .map(Document::getText)
        .collect(Collectors.joining("\n"));

    // 2. Augment the prompt
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(PROMPT_TEMPLATE);
    Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", context));
    Message userMessage = new UserMessage(input.getMessage());
    Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

    // 3. Generate the response
    String message = chatClient.prompt(prompt)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "fc0efe70-1d2c-4841-aaf6-68be457a1336"))
        .call()
        .content();

    return new SendMessageOutput(message);
  }
}
