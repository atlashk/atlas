package org.atlas.services.notification.infrastructure.api.server.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.config.AppStackConfigService;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.notification.infrastructure.api.server.rest.mapper.NotificationMapper;
import org.atlas.services.notification.infrastructure.api.server.rest.model.InAppNotificationResponse;
import org.atlas.services.notification.infrastructure.api.server.rest.model.InAppServiceInfoResponse;
import org.atlas.services.notification.port.in.model.MarkAsReadAllInput;
import org.atlas.services.notification.port.in.model.RetrieveInAppNotificationListInput;
import org.atlas.services.notification.port.in.service.InAppNotificationService;
import org.atlas.services.notification.domain.entity.Notification;
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
  private final AppStackConfigService appStackConfigService;

  @GetMapping(value = "/service-info", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get current in-app service name")
  public ApiResponseWrapper<InAppServiceInfoResponse> retrieveCurrentInAppServiceType() {
    String serviceName = appStackConfigService.getServiceName("notification.in-app");
    InAppServiceInfoResponse responseData = InAppServiceInfoResponse.builder()
        .serviceName(serviceName.toLowerCase())
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
    List<InAppNotificationResponse> responseData = MapperUtil.mapList(notifications,
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