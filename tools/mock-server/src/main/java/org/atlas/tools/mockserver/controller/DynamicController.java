package org.atlas.tools.mockserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.tools.mockserver.model.EndpointDefinition;
import org.atlas.tools.mockserver.model.RequestDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DynamicController {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final List<EndpointDefinition> endpointDefinitions;
  private final RequestMappingHandlerMapping handlerMapping;

  @PostConstruct
  public void initializeEndpoints() throws NoSuchMethodException {
    log.info("Initializing dynamic endpoints...");
    for (EndpointDefinition def : endpointDefinitions) {
      RequestMappingInfo mappingInfo = createMappingInfo(def);
      Method method = DynamicController.class.getMethod("handleDynamicRequest",
          String.class, Map.class, Map.class);
      handlerMapping.registerMapping(mappingInfo, this, method);
      log.debug("Registered endpoint: {} {}", def.getMethod(), def.getPath());
    }
    log.info("Endpoint initialization completed. Registered {} endpoints.", endpointDefinitions.size());
  }

  private RequestMappingInfo createMappingInfo(EndpointDefinition def) {
    RequestMappingInfo.Builder builder = RequestMappingInfo
        .paths(def.getPath())
        .methods(RequestMethod.valueOf(def.getMethod()));

    if ("query".equals(def.getRequest().getType()) && def.getRequest().getParameters() != null) {
      builder.params(def.getRequest().getParameters().stream()
          .map(param -> param.getName() + "=")
          .toArray(String[]::new));
    }

    return builder.build();
  }

  public ResponseEntity<String> handleDynamicRequest(
      @PathVariable(required = false) String pathVar,
      @RequestParam(required = false) Map<String, String> queryParams,
      @RequestBody(required = false) Map<String, Object> body) {
    // Log request details
    try {
      StringBuilder requestLog = new StringBuilder();
      requestLog.append("Handling request for path: ").append(getCurrentPath());
      requestLog.append(", Method: ").append(getCurrentMethod());
      if (pathVar != null) {
        requestLog.append(", Path Variable: ").append(pathVar);
      }
      if (queryParams != null && !queryParams.isEmpty()) {
        requestLog.append(", Query Parameters: ").append(queryParams);
      }
      if (body != null) {
        requestLog.append(", Request Body: ").append(objectMapper.writeValueAsString(body));
      }
      log.info(requestLog.toString());
    } catch (Exception e) {
      log.error("Failed to log request details: {}", e.getMessage());
    }

    for (EndpointDefinition def : endpointDefinitions) {
      String responseBody = def.getResponse().getBody();
      RequestDefinition request = def.getRequest();

      // Handle path variable placeholders only
      if ("path".equals(request.getType()) && pathVar != null && request.getParameters() != null) {
        responseBody = responseBody.replace("{" + request.getParameters().get(0).getName() + "}", pathVar);
      }

      // Log response details
      try {
        log.info("Returning response for {} {}: Status: {}, Body: {}",
            def.getMethod(), def.getPath(), def.getResponse().getStatus(), responseBody);
      } catch (Exception e) {
        log.error("Failed to log response details: {}", e.getMessage());
      }

      return ResponseEntity
          .status(def.getResponse().getStatus())
          .body(responseBody);
    }

    // Log not found
    log.warn("No matching endpoint found for path: {}, method: {}", getCurrentPath(), getCurrentMethod());
    return ResponseEntity.notFound().build();
  }

  // Helper methods for logging
  private String getCurrentPath() {
    return endpointDefinitions.stream()
        .filter(def -> def.getPath().equals(getCurrentRequestPath()))
        .findFirst()
        .map(EndpointDefinition::getPath)
        .orElse("unknown");
  }

  private String getCurrentMethod() {
    return endpointDefinitions.stream()
        .filter(def -> def.getPath().equals(getCurrentRequestPath()))
        .findFirst()
        .map(EndpointDefinition::getMethod)
        .orElse("unknown");
  }

  private String getCurrentRequestPath() {
    return endpointDefinitions.get(0).getPath(); // Placeholder; consider injecting HttpServletRequest
  }
}
