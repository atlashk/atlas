package org.atlas.services.catalog.infrastructure.ai.chatbot.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatbotConfig {

  @Bean
  public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(jdbcChatMemoryRepository)  // Database persistence!
        .maxMessages(10)  // Keep last 10 messages per conversation
        .build();
  }
}
