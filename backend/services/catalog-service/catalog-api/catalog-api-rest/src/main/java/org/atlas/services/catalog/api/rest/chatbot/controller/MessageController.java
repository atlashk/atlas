package org.atlas.services.catalog.api.rest.chatbot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.paging.PagingRequest;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.catalog.api.rest.chatbot.mapper.MessageMapper;
import org.atlas.services.catalog.api.rest.chatbot.model.MessageResponse;
import org.atlas.services.catalog.api.rest.chatbot.model.RetrieveMessageListRequest;
import org.atlas.services.catalog.api.rest.chatbot.model.SendMessageRequest;
import org.atlas.services.catalog.api.rest.chatbot.model.SendMessageResponse;
import org.atlas.services.catalog.domain.entity.chatbot.MessageEntity;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageInput;
import org.atlas.services.catalog.port.in.chatbot.model.SendMessageOutput;
import org.atlas.services.catalog.port.in.chatbot.service.MessageService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot/messages")
@Validated
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve message list by conversation")
  public ApiResponseWrapper<List<MessageResponse>> retrieveMessageList(
      @Parameter(description = "Request object containing conversation ID and pagination", required = true)
      @Valid @RequestBody RetrieveMessageListRequest request
  ) {
    PagingRequest pagingRequest = PagingRequest.of(request.getPage() - 1, request.getSize());
    List<MessageEntity> messages = messageService.retrieveMessageList(request.getConversationId(),
        pagingRequest);
    List<MessageResponse> responseData = MapperUtil.mapList(messages,
        MessageMapper.INSTANCE::toMessageResponse);
    return ApiResponseWrapper.success(responseData);
  }

  @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Send a message to chatbot")
  public ApiResponseWrapper<SendMessageResponse> sendMessage(
      @Parameter(description = "Request object containing message data", required = true)
      @Valid @RequestBody SendMessageRequest request
  ) {
    SendMessageInput input = MessageMapper.INSTANCE.toSendMessageInput(request);
    SendMessageOutput output = messageService.sendMessage(input);
    SendMessageResponse responseData = MessageMapper.INSTANCE.toSendMessageResponse(output);
    return ApiResponseWrapper.success(responseData);
  }

  @DeleteMapping(value = "/conversation/{conversationId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete all messages in a conversation")
  public ApiResponseWrapper<Void> deleteAllMessages(
      @Parameter(name = "conversationId", description = "The unique identifier of the conversation", example = "01HV4Y7G2D2Q17W0P1D3YH7G8N", required = true)
      @PathVariable String conversationId) {
    messageService.deleteAllMessages(conversationId);
    return ApiResponseWrapper.success();
  }
}
