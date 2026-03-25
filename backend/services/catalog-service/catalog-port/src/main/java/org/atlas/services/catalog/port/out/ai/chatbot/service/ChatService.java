package org.atlas.services.catalog.port.out.ai.chatbot.service;

import org.atlas.services.catalog.port.out.ai.chatbot.model.SendMessageInput;
import org.atlas.services.catalog.port.out.ai.chatbot.model.SendMessageOutput;

public interface ChatService {

  SendMessageOutput sendMessage(SendMessageInput input);
}
