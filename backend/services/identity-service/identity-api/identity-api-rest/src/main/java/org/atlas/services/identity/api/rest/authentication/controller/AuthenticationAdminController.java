package org.atlas.services.identity.api.rest.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.services.identity.api.rest.authentication.model.ResetPasswordRequest;
import org.atlas.services.identity.port.in.authentication.service.AuthenticationAdminService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authentication/admin")
@Validated
@RequiredArgsConstructor
public class AuthenticationAdminController {

  private final AuthenticationAdminService authenticationAdminService;

  @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Reset user password by admin")
  public ApiResponseWrapper<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request)
      throws Exception {
    authenticationAdminService.resetPassword(request.getUserId());
    return ApiResponseWrapper.success();
  }
}
