package org.atlas.services.identity.api.rest.authentication.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request object for admin resetting a user password")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResetPasswordRequest {

  @NotBlank
  @Schema(description = "User ID to reset password", example = "1", requiredMode = RequiredMode.REQUIRED)
  private String userId;
}
