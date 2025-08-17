package org.atlas.infrastructure.api.server.rest.core.document;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.config.ApplicationConfigPort;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

  private final ApplicationConfigPort applicationConfigPort;

  private static final String DEFAULT_MEDIA_TYPE = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

  // Map to store status codes and their example responses
  private static final Map<String, String> RESPONSE_EXAMPLES = Map.of(
      "400", "{\"success\": false, \"code\": \"1002\", \"message\": \"Bad request\"}",
      "500", "{ \"success\": false, \"code\": \"1000\", \"message\": \"An unexpected error occurred\"}"
  );

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title(applicationConfigPort.getApplicationName() + " API Docs")
            .version("1.0")
            .contact(new Contact()
                .name("Your Name")
                .email("your.email@example.com")
                .url("https://your-website.com"))
            .license(new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
  }

  @Bean
  public OpenApiCustomizer openApiCustomizer() {
    return openApi ->
        openApi.getPaths().values().forEach(pathItem ->
            pathItem.readOperations().forEach(operation ->
                operation.getResponses().forEach((statusCode, response) -> {
                  // Apply example if we have one defined for this status code
                  if (RESPONSE_EXAMPLES.containsKey(statusCode)) {
                    // For 200 responses, just add examples without replacing media types
                    // For non-200 responses, replace */* with application/json and add examples
                    customizeResponse(response, RESPONSE_EXAMPLES.get(statusCode));
                  }
                })
            )
        );
  }

  /**
   * Adds examples and replaces *\/* with application/json
   */
  private void customizeResponse(ApiResponse response, String exampleValue) {
    if (response.getContent() != null) {
      // First add examples to all media types
      response.getContent().forEach((contentType, mediaType) -> {
        Example example = new Example().value(exampleValue);
        mediaType.addExamples("default", example);
      });

      // Then look specifically for */* to replace it
      if (response.getContent().containsKey("*/*")) {
        // Get the media type for */*
        MediaType mediaType = response.getContent().get("*/*");

        // Remove */* entry
        response.getContent().remove("*/*");

        // Add with application/json if it doesn't already exist
        if (!response.getContent().containsKey(DEFAULT_MEDIA_TYPE)) {
          response.getContent().put(DEFAULT_MEDIA_TYPE, mediaType);
        }
      }
    }
  }
}
