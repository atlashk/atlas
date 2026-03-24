package org.atlas.services.catalog.infrastructure.ai.rag;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.catalog.port.out.ai.rag.model.ChatInput;
import org.atlas.services.catalog.port.out.ai.rag.service.ProductRagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductRagServiceImpl implements ProductRagService {

  private final ChatClient chatClient;
  private final VectorStore vectorStore;

  public ProductRagServiceImpl(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
    this.chatClient = chatClientBuilder.build();
    this.vectorStore = vectorStore;
  }

  private static final String PROMPT_TEMPLATE = """
      You are a helpful assistant. Use the following information to answer the question in detail. Please use a friendly and professional tone. Please acknowledge the question and relate the answer back to it.\s
      If the answer is not in the provided information, say "I don't know."
      
      Information:
      {context}
      
      Answer:
      """;

//  public String chat(ChatInput input) {
//    // Build prompt from template
//    PromptTemplate customPromptTemplate = PromptTemplate.builder()
//        .renderer(StTemplateRenderer.builder()
//            .startDelimiterToken('<')
//            .endDelimiterToken('>')
//            .build())
//        .template("""
//            <query>
//
//            Context information is below.
//
//            ---------------------
//            <question_answer_context>
//            ---------------------
//
//            Given the context information and no prior knowledge, answer the query.
//
//            Follow these rules:
//
//            1. If the answer is not in the context, just say that you don't know.
//            2. Avoid statements like "Based on the context..." or "The provided information...".
//            """)
//        .build();
//
//    // Search request
//    SearchRequest.Builder searchRequestBuilder = SearchRequest.builder();
//    if (input.getTopK() != null) {
//      searchRequestBuilder.topK(input.getTopK());
//    }
//    if (input.getSimilarityThreshold() != null) {
//      searchRequestBuilder.similarityThreshold(input.getSimilarityThreshold());
//    }
//    SearchRequest searchRequest = searchRequestBuilder.build();
//
//    // Debug vector store
//    SearchRequest searchRequest2 = SearchRequest.builder()
//        .query(input.getQuestion())
//        .topK(input.getTopK())
//        .similarityThreshold(input.getSimilarityThreshold())
//        .build();
//    List<Document> docs = vectorStore.similaritySearch(searchRequest2);
//    log.info("Found {} document", docs.size());
//    for (Document document : docs) {
//      log.info("Found document: {}", document.getMetadata().get("productId"));
//    }
//
//    // Advisor
//    QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
//        .promptTemplate(customPromptTemplate)
//        .searchRequest(searchRequest)
//        .build();
//
//    return ChatClient.builder(chatModel).build()
//        .prompt(input.getQuestion())
//        .advisors(qaAdvisor)
//        .call()
//        .content();
//  }

  public String chat(ChatInput input) {
    // 1. Retrieve similar documents
    SearchRequest searchRequest = SearchRequest.builder()
        .query(input.getQuestion())
        .topK(input.getTopK())
        .similarityThreshold(input.getSimilarityThreshold())
        .build();
    List<Document> retrievedDocs = vectorStore.similaritySearch(searchRequest);
    log.info("Retrieved {} documents", retrievedDocs.size());
    if (CollectionUtil.isEmpty(retrievedDocs)) {
      return "Not found matches products";
    }
    // Debug
    for (Document retrievedDoc : retrievedDocs) {
      log.info("Retrieved document: {}", retrievedDoc.getMetadata().get("productId"));
    }
    String context = retrievedDocs.stream()
        .map(Document::getText)
        .collect(Collectors.joining("\n"));

    // 2. Augment the prompt
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(PROMPT_TEMPLATE);
    Prompt prompt = new Prompt(List.of(
        systemPromptTemplate.createMessage(Map.of("context", context)),
        new UserMessage(input.getQuestion())));

    // 3. Generate the response
    return chatClient.prompt(prompt).call().content();
  }
}
