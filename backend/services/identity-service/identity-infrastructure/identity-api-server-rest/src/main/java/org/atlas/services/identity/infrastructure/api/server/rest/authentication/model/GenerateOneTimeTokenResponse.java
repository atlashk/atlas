package org.atlas.services.identity.infrastructure.api.server.rest.authentication.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Response object for user generate one-time token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOneTimeTokenResponse {

  @Schema(description = "One-time token")
  private String token;
}
