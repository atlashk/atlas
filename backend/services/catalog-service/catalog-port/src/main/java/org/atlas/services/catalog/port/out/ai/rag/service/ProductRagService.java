package org.atlas.services.catalog.port.out.ai.rag.service;

import org.atlas.services.catalog.port.out.ai.rag.model.ChatInput;

public interface ProductRagService {

  String chat(ChatInput input);
}
