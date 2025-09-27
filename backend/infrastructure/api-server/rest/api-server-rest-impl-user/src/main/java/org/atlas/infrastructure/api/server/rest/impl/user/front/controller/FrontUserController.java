<<<<<<<< HEAD:backend/infrastructure/api-server/rest/api-server-rest-impl-user/src/main/java/org/atlas/infrastructure/api/server/rest/impl/user/common/controller/CommonUserController.java
package org.atlas.infrastructure.api.server.rest.impl.user.common.controller;
========
package org.atlas.infrastructure.api.server.rest.impl.user.front.controller;
>>>>>>>> 6eb937f1a05c7fa83208d5ad3451dcd4c6024e8a:backend/infrastructure/api-server/rest/api-server-rest-impl-user/src/main/java/org/atlas/infrastructure/api/server/rest/impl/user/front/controller/FrontUserController.java

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.usecase.front.handler.FrontRegisterUseCaseHandler;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.infrastructure.api.server.rest.impl.user.front.model.FrontRegisterRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/front/user")
@Validated
@RequiredArgsConstructor
<<<<<<<< HEAD:backend/infrastructure/api-server/rest/api-server-rest-impl-user/src/main/java/org/atlas/infrastructure/api/server/rest/impl/user/common/controller/CommonUserController.java
public class CommonUserController {

  private final GetProfileUseCaseHandler getProfileUseCaseHandler;

  @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user profile")
  public ApiResponseWrapper<UserResponse> getProfile() throws Exception {

    UserEntity userEntity = getProfileUseCaseHandler.handle(null);
    UserResponse userResponse = ObjectMapperUtil.getInstance()
        .map(userEntity, UserResponse.class);
    return ApiResponseWrapper.success(userResponse);
========
public class FrontUserController {

  private final FrontRegisterUseCaseHandler frontRegisterUseCaseHandler;

  @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "User registration")
  public ApiResponseWrapper<Void> register(
      @Parameter(description = "Request object containing the needed information to register a user", required = true)
      @Valid @RequestBody FrontRegisterRequest request) throws Exception {
    RegisterInput input = ObjectMapperUtil.getInstance()
        .map(request, RegisterInput.class);
    frontRegisterUseCaseHandler.handle(input);
    return ApiResponseWrapper.success();
>>>>>>>> 6eb937f1a05c7fa83208d5ad3451dcd4c6024e8a:backend/infrastructure/api-server/rest/api-server-rest-impl-user/src/main/java/org/atlas/infrastructure/api/server/rest/impl/user/front/controller/FrontUserController.java
  }
}
