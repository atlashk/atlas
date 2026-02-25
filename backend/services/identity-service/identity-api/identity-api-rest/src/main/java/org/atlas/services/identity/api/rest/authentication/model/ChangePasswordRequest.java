package org.atlas.services.identity.api.rest.authentication.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.constant.Patterns;

@Schema(description = "Request object for changing user password")
@Getter
@Setter
public class ChangePasswordRequest {

  @NotBlank
  @Schema(description = "Current password of the user", example = "P@ssw0rd", requiredMode = RequiredMode.REQUIRED)
  private String oldPassword;

  @NotBlank
  @Pattern(regexp = Patterns.PASSWORD)
  @Schema(description = "New password for the user, must meet security requirements", example = "P@ssw0rd123", requiredMode = RequiredMode.REQUIRED)
  private String newPassword;
}
