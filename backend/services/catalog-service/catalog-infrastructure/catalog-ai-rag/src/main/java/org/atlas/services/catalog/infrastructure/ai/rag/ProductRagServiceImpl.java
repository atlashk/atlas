package org.atlas.services.catalog.infrastructure.ai.rag;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.services.catalog.port.out.ai.rag.model.ChatInput;
import org.atlas.services.catalog.port.out.ai.rag.service.ProductRagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductRagServiceImpl implements ProductRagService {

  private final ChatModel chatModel;
  private final VectorStore vectorStore;

  public String chat(ChatInput input, String promptTemplateStr) {
    // Build prompt from template
    PromptTemplate promptTemplate = PromptTemplate.builder()
        .template(promptTemplateStr)
        .build();
    Prompt prompt = promptTemplate.create(
        Map.of("input", input.getQuestion())
    );

    // Search request
    SearchRequest.Builder searchRequestBuilder = SearchRequest.builder();
    if (input.getTopK() != null) {
      searchRequestBuilder.topK(input.getTopK());
    }
    if (input.getSimilarityThreshold() != null) {
      searchRequestBuilder.similarityThreshold(input.getSimilarityThreshold());
    }
    SearchRequest searchRequest = searchRequestBuilder.build();

    // Advisor
    QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(searchRequest)
        .build();

    return ChatClient.builder(chatModel).build()
        .prompt(prompt)
        .advisors(advisor)
        .call()
        .content();
  }
}
