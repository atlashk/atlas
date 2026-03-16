package org.atlas.libs.api.server.rest.referencedata;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference-data")
@Validated
@RequiredArgsConstructor
public class ReferenceDataController {

  private final ReferenceDataService referenceDataService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Retrieve reference data by type")
  public ApiResponseWrapper<Map<String, String>> retrieveReferenceData(
      @Parameter(name = "type", description = "Type of reference data", example = "ORDER_STATUS", required = true)
      @RequestParam String type) {
    Map<String, String> responseData = referenceDataService.retrieveReferenceData(type);
    if (responseData == null) {
      throw new BaseDomainException(CommonDomainError.BAD_REQUEST, "Unknown reference data type: " + type);
    }
    return ApiResponseWrapper.success(responseData);
  }
}
