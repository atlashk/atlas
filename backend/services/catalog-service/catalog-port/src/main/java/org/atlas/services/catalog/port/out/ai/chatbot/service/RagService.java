package org.atlas.services.catalog.port.out.ai.chatbot.service;

import org.atlas.services.catalog.port.out.ai.chatbot.model.ChatInput;
import org.atlas.services.catalog.port.out.ai.chatbot.model.ChatOutput;

public interface RagService {

  ChatOutput chat(ChatInput input);
}
