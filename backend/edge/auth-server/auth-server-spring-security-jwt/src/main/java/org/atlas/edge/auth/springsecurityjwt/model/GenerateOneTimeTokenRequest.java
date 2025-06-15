package org.atlas.edge.auth.springsecurityjwt.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for user generate one-time token.")
public class GenerateOneTimeTokenRequest {

  @NotBlank
  @Schema(description = "Username, email, of phone number of the user attempting to log in.", example = "john_doe")
  private String identifier;
}
