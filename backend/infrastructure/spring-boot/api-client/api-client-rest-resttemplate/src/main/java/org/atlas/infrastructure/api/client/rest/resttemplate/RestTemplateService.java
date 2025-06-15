package org.atlas.infrastructure.api.client.rest.resttemplate;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class RestTemplateService {

  private final RestTemplate restTemplate;

  public <Res> Res doGet(String url, Map<String, String> paramsMap, Map<String, String> headersMap,
      Class<Res> responseClass) {
    final String finalUrl = buildUrl(url, paramsMap);
    HttpHeaders requestHeaders = buildHttpHeaders(headersMap);
    HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);
    ResponseEntity<Res> response = restTemplate.exchange(finalUrl, HttpMethod.GET, requestEntity,
        responseClass);
    return response.getBody();
  }

  public <Req, Res> Res doPost(String url, Map<String, String> headersMap, Req requestBody,
      Class<Res> responseClass) {
    HttpHeaders requestHeaders = buildHttpHeaders(headersMap);
    HttpEntity<Req> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
    ResponseEntity<Res> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
        responseClass);
    return response.getBody();
  }

  public <Res> Res doPost(String url, Map<String, String> headersMap, Map<String, String> formData,
      Class<Res> responseClass) {
    HttpHeaders requestHeaders = buildHttpHeaders(headersMap);
    MultiValueMap<String, String> multiValueMap = convertToMultiValueMap(formData);
    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(multiValueMap,
        requestHeaders);
    ResponseEntity<Res> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
        responseClass);
    return response.getBody();
  }

  public <Req, Res> Res doPut(String url, Map<String, String> headersMap, Req requestBody,
      Class<Res> responseClass) {
    HttpHeaders requestHeaders = buildHttpHeaders(headersMap);
    HttpEntity<Req> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
    ResponseEntity<Res> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity,
        responseClass);
    return response.getBody();
  }

  public <Res> Res doDelete(String url, Map<String, String> headersMap, Class<Res> responseClass) {
    HttpHeaders requestHeaders = buildHttpHeaders(headersMap);
    HttpEntity<String> requestEntity = new HttpEntity<>(requestHeaders);
    ResponseEntity<Res> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity,
        responseClass);
    return response.getBody();
  }

  private String buildUrl(String url, Map<String, String> paramsMap) {
    if (paramsMap == null || paramsMap.isEmpty()) {
      return url;
    }
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
    paramsMap.forEach(builder::queryParam);
    return builder.toUriString();
  }

  private HttpHeaders buildHttpHeaders(Map<String, String> headersMap) {
    HttpHeaders headers = null;
    if (headersMap != null && !headersMap.isEmpty()) {
      headers = new HttpHeaders();
      headersMap.forEach(headers::add);
    }
    return headers;
  }

  private MultiValueMap<String, String> convertToMultiValueMap(Map<String, String> data) {
    MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
    data.forEach(multiValueMap::add);
    return multiValueMap;
  }
}
