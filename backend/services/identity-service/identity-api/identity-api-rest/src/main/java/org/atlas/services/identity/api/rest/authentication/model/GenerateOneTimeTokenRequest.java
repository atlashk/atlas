package org.atlas.services.identity.api.rest.authentication.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request object for user generate one-time token")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GenerateOneTimeTokenRequest {

  @NotBlank
  @Schema(description = "Email of the user requesting a one-time token", example = "john.doe@example.com", requiredMode = RequiredMode.REQUIRED)
  private String email;
}
