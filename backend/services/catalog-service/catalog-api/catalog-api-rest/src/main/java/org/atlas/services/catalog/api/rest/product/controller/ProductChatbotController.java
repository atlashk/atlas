package org.atlas.services.catalog.api.rest.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.services.catalog.port.out.ai.chatbot.model.SendMessageInput;
import org.atlas.services.catalog.port.out.ai.chatbot.model.SendMessageOutput;
import org.atlas.services.catalog.port.out.ai.chatbot.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/chatbot")
@Validated
@RequiredArgsConstructor
public class ProductChatbotController {

  private final ChatService chatService;

  @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Chat with product RAG service")
  public ApiResponseWrapper<String> chat(
      @Parameter(description = "Request object containing chat input", required = true)
      @RequestBody SendMessageInput input
  ) {
    SendMessageOutput responseData = chatService.sendMessage(input);
    return ApiResponseWrapper.success(responseData.getMessage());
  }
}
