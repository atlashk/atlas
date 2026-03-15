package org.atlas.platform.authorization.spring.api.rest.user.model.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.constant.CommonConstant;
import org.atlas.libs.framework.domain.shared.identity.UserRole;

@Schema(description = "Request object for retrieving user list")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetrieveUserListRequest {

  @Schema(description = "ID", example = "1")
  private String id;

  @Schema(description = "First name", example = "John")
  private String firstName;

  @Schema(description = "Last name", example = "Doe")
  private String lastName;

  @Schema(description = "Email", example = "johndoe@example.com")
  private String email;

  @Schema(description = "Phone number", example = "+1234567890")
  private String phone;

  @Schema(description = "User role", example = "USER")
  private UserRole role;

  @Positive
  @Min(1)
  @Schema(description = "The page number", example = "1", defaultValue = "1")
  @Builder.Default
  private int page = 1;

  @Positive
  @Min(0)
  @Schema(description = "The number of records per page", example = "20", defaultValue = "20")
  @Builder.Default
  private int size = CommonConstant.DEFAULT_PAGE_SIZE;
}
