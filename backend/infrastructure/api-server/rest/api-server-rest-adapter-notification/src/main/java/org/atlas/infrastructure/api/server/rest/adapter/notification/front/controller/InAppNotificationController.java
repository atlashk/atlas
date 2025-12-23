package org.atlas.infrastructure.api.server.rest.adapter.notification.front.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.application.notification.model.MarkAsReadAllInput;
import org.atlas.application.notification.model.RetrieveInAppNotificationListInput;
import org.atlas.application.notification.service.InAppNotificationService;
import org.atlas.domain.notification.entity.Notification;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.util.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.adapter.notification.front.mapper.NotificationMapper;
import org.atlas.infrastructure.api.server.rest.adapter.notification.front.model.InAppNotificationResponse;
import org.atlas.infrastructure.api.server.rest.adapter.notification.front.model.InAppServiceInfoResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/inapp")
@Validated
@RequiredArgsConstructor
public class InAppNotificationController {

  private final InAppNotificationService inAppNotificationService;

  @GetMapping(value = "/service-info", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get current in-app service type information")
  public ApiResponseWrapper<InAppServiceInfoResponse> retrieveCurrentInAppServiceType() {
    String serviceType = inAppNotificationService.retrieveCurrentInAppServiceType();
    InAppServiceInfoResponse responseData = InAppServiceInfoResponse.builder()
        .serviceType(serviceType.toLowerCase())
        .build();
    return ApiResponseWrapper.success(responseData);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve a list of in-app notifications for the current user")
  public ApiResponseWrapper<List<InAppNotificationResponse>> retrieveInAppNotification(
      @Parameter(name = "page", description = "The page number to retrieve (default is 1).", example = "1")
      @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit
  ) {
    RetrieveInAppNotificationListInput input = RetrieveInAppNotificationListInput.builder()
        .userId(Contexts.getUserId())
        .limit(limit)
        .build();
    List<Notification> notifications = inAppNotificationService.retrieveInAppNotification(input);
    List<InAppNotificationResponse> responseData = ObjectMapperUtil.mapList(notifications,
        NotificationMapper.INSTANCE::toInAppNotificationResponse);
    return ApiResponseWrapper.success(responseData);
  }

  @PostMapping(value = "/mark-all-as-read", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Mark all in-app notifications as read for the current user")
  public ApiResponseWrapper<Void> markAllAsRead() {
    MarkAsReadAllInput input = MarkAsReadAllInput.builder()
        .userId(Contexts.getUserId())
        .build();
    inAppNotificationService.markAsReadAll(input);
    return ApiResponseWrapper.success();
  }
}