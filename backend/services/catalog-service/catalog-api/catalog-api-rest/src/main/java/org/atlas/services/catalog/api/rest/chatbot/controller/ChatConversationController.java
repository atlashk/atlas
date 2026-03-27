package org.atlas.services.catalog.api.rest.chatbot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.paging.PagingRequest.SortOrder;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.api.rest.chatbot.mapper.ChatConversationMapper;
import org.atlas.services.catalog.api.rest.chatbot.mapper.ChatMessageMapper;
import org.atlas.services.catalog.api.rest.chatbot.model.ChatConversationResponse;
import org.atlas.services.catalog.api.rest.chatbot.model.RetrieveChatConversationListRequest;
import org.atlas.services.catalog.api.rest.chatbot.model.SendChatMessageRequest;
import org.atlas.services.catalog.api.rest.chatbot.model.SendChatMessageResponse;
import org.atlas.services.catalog.domain.entity.chatbot.ChatConversationEntity;
import org.atlas.services.catalog.port.in.chatbot.model.ChatSendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;
import org.atlas.services.catalog.port.in.chatbot.service.ChatConversationService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot/conversations")
@Validated
@RequiredArgsConstructor
public class ChatConversationController {

  private final ChatConversationService chatConversationService;

  @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve conversation list of current user")
  public ApiResponseWrapper<List<ChatConversationResponse>> retrieveConversationList(
      @Parameter(description = "Request object containing pagination", required = true)
      @Valid @RequestBody RetrieveChatConversationListRequest request
  ) {
    PagingRequest pagingRequest = PagingRequest.of(request.getPage() - 1, request.getSize(),
        "createdAt", SortOrder.DESC);
    List<ChatConversationEntity> conversations = chatConversationService.retrieveConversationList(
        pagingRequest);
    List<ChatConversationResponse> responseData = MapperUtil.mapList(conversations,
        ChatConversationMapper.INSTANCE::toConversationResponse);
    return ApiResponseWrapper.success(responseData);
  }

  @PostMapping(value = "/start", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Start a new conversation")
  public ApiResponseWrapper<SendChatMessageResponse> startConversation(
      @Parameter(description = "Request object containing first user message", required = true)
      @Valid @RequestBody SendChatMessageRequest request
  ) {
    ChatSendMessageInput input = ChatMessageMapper.INSTANCE.toSendMessageInput(request);
    SendMessageOutput output = chatConversationService.startConversation(input);
    SendChatMessageResponse responseData = ChatMessageMapper.INSTANCE.toSendMessageResponse(output);
    return ApiResponseWrapper.success(responseData);
  }

  @DeleteMapping(value = "/{conversationId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete a conversation and all its messages")
  public ApiResponseWrapper<Void> deleteConversation(
      @Parameter(name = "conversationId", description = "The unique identifier of the conversation", example = "01HV4Y7G2D2Q17W0P1D3YH7G8N", required = true)
      @PathVariable String conversationId) {
    chatConversationService.deleteConversation(conversationId);
    return ApiResponseWrapper.success();
  }
}
