package org.atlas.infrastructure.auth.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Schema(description = "Response object for user generate one-time token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOneTimeTokenResponse {

  private String token;
}
