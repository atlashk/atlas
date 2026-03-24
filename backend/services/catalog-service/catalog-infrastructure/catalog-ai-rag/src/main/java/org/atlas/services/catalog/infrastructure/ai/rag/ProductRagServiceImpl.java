package org.atlas.services.catalog.infrastructure.ai.rag;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.services.catalog.port.out.ai.rag.model.ChatInput;
import org.atlas.services.catalog.port.out.ai.rag.service.ProductRagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "product-rag")
public class ProductRagServiceImpl implements ProductRagService {

  private final ChatModel chatModel;
  private final VectorStore vectorStore;

  public String chat(ChatInput input) {
    // Build prompt from template
    PromptTemplate customPromptTemplate = PromptTemplate.builder()
        .renderer(StTemplateRenderer.builder()
            .startDelimiterToken('<')
            .endDelimiterToken('>')
            .build())
        .template("""
            <query>
            
            Context information is below.
            
            ---------------------
            <question_answer_context>
            ---------------------
            
            Given the context information and no prior knowledge, answer the query.
            
            Follow these rules:
            
            1. If the answer is not in the context, just say that you don't know.
            2. Avoid statements like "Based on the context..." or "The provided information...".
            """)
        .build();

    // Search request
    SearchRequest.Builder searchRequestBuilder = SearchRequest.builder();
    if (input.getTopK() != null) {
      searchRequestBuilder.topK(input.getTopK());
    }
    if (input.getSimilarityThreshold() != null) {
      searchRequestBuilder.similarityThreshold(input.getSimilarityThreshold());
    }
    SearchRequest searchRequest = searchRequestBuilder.build();

    // Debug vector store
    SearchRequest searchRequest2 = SearchRequest.builder()
        .query(input.getQuestion())
        .topK(input.getTopK())
        .similarityThreshold(input.getSimilarityThreshold())
        .build();
    List<Document> docs = vectorStore.similaritySearch(searchRequest2);
    log.info("Found {} document", docs.size());
    for (Document document : docs) {
      log.info("Found document: {}", document.getMetadata().get("productId"));
    }

    // Advisor
    QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
        .promptTemplate(customPromptTemplate)
        .searchRequest(searchRequest)
        .build();

    return ChatClient.builder(chatModel).build()
        .prompt(input.getQuestion())
        .advisors(qaAdvisor)
        .call()
        .content();
  }
}
