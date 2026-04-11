package org.atlas.services.user.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.services.user.port.in.service.UserInitializerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/admin/initialize")
@RequiredArgsConstructor
public class UserInitializerController {

  private final UserInitializerService userInitializerService;

  @PostMapping(value = "/user-data", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @Operation(summary = "Trigger async initialization of default user data")
  public ApiResponseWrapper<Void> initializeUserData() throws Exception {
    userInitializerService.initializeUserData();
    return ApiResponseWrapper.success();
  }
}
