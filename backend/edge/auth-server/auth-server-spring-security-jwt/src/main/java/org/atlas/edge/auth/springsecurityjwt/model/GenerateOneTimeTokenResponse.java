package org.atlas.edge.auth.springsecurityjwt.model;

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

  private String token;
}
