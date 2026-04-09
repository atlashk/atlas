package org.atlas.services.catalog.infrastructure.ai.chatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatConfig {

  @Bean
  @Primary
  public EmbeddingModel embeddingModel(EmbeddingModel openAiEmbeddingModel) {
    return openAiEmbeddingModel;
  }

  @Bean
  @Primary
  public ChatModel chatModel(ChatModel openAiChatModel) {
    return openAiChatModel;
  }

  @Bean
  public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(jdbcChatMemoryRepository)  // Database persistence!
        .maxMessages(10)  // Keep last 10 messages per conversation
        .build();
  }

  @Bean
  public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
    return chatClientBuilder
        .defaultAdvisors(
            SimpleLoggerAdvisor.builder().build(), // Logging
            MessageChatMemoryAdvisor.builder(chatMemory).build() // Chat-memory advisor
        )
        .build();
  }
}
