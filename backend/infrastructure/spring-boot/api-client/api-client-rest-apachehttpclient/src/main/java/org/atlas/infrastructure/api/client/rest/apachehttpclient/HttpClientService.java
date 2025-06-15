package org.atlas.infrastructure.api.client.rest.apachehttpclient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.atlas.framework.json.JsonUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HttpClientService implements DisposableBean {

  private final CloseableHttpClient httpClient;

  public HttpClientService() {
    this.httpClient = HttpClientFactory.custom();
  }

  @Override
  public void destroy() {
    try {
      this.httpClient.close();
      log.info("Closed HttpClient");
    } catch (IOException e) {
      log.error("Failed to close HttpClient", e);
    }
  }

  public <Res> Res doGet(String url, Map<String, String> paramsMap, Map<String, String> headersMap,
      Class<Res> responseClass) {
    final String finalUrl = buildUrl(url, paramsMap);
    HttpGet httpRequest = new HttpGet(finalUrl);
    setHeaders(httpRequest, headersMap);
    return call(httpRequest, responseClass);
  }

  /**
   * Send request body as JSON string
   */
  public <Req, Res> Res doPost(String url, Map<String, String> headersMap, Req requestBody,
      Class<Res> responseClass) {
    HttpPost httpRequest = new HttpPost(url);
    setHeaders(httpRequest, headersMap);
    httpRequest.addHeader("Content-Type", "application/json");
    setRequestBodyAsJson(httpRequest, requestBody);
    return call(httpRequest, responseClass);
  }

  /**
   * Send request body as URL-encoded form data
   */
  public <Res> Res doPost(String url, Map<String, String> headersMap, Map<String, String> formData,
      Class<Res> responseClass) {
    HttpPost httpRequest = new HttpPost(url);
    setHeaders(httpRequest, headersMap);
    setRequestBodyAsUrlEncoded(httpRequest, formData);
    httpRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
    return call(httpRequest, responseClass);
  }

  public <Req, Res> Res doPut(String url, Map<String, String> headersMap, Req requestBody,
      Class<Res> responseClass) {
    HttpPut httpRequest = new HttpPut(url);
    setHeaders(httpRequest, headersMap);
    httpRequest.addHeader("Content-Type", "application/json");
    setRequestBodyAsJson(httpRequest, requestBody);
    return call(httpRequest, responseClass);
  }

  public <Res> Res doDelete(String url, Map<String, String> headersMap, Class<Res> responseClass) {
    HttpDelete httpRequest = new HttpDelete(url);
    setHeaders(httpRequest, headersMap);
    return call(httpRequest, responseClass);
  }

  private String buildUrl(String url, Map<String, String> paramsMap) {
    StringBuilder builder = new StringBuilder(url);
    if (MapUtils.isNotEmpty(paramsMap)) {
      builder.append("?");
      paramsMap.forEach((paramName, paramValue) ->
          builder.append(paramName).append("=").append(paramValue).append("&"));
      // Remove last '&'
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  private void setHeaders(HttpUriRequestBase httpRequest, Map<String, String> headersMap) {
    if (MapUtils.isNotEmpty(headersMap)) {
      headersMap.forEach(httpRequest::addHeader);
    }
  }

  private <Req> void setRequestBodyAsJson(HttpUriRequestBase httpRequest, Req requestBody) {
    if (requestBody == null) {
      return;
    }
    String requestBodyJson = JsonUtil.getInstance()
        .toJson(requestBody);
    StringEntity httpEntity = new StringEntity(requestBodyJson, StandardCharsets.UTF_8);
    httpRequest.setEntity(httpEntity);
  }

  private void setRequestBodyAsUrlEncoded(HttpUriRequestBase httpRequest,
      Map<String, String> data) {
    if (MapUtils.isEmpty(data)) {
      return;
    }
    List<NameValuePair> formData = new ArrayList<>();
    data.forEach((name, value) -> formData.add(new BasicNameValuePair(name, value)));
    UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(formData);
    httpRequest.setEntity(httpEntity);
  }

  private <Res> Res call(ClassicHttpRequest httpRequest, Class<Res> responseClass) {
    try (CloseableHttpResponse httpResponse = httpClient.execute(httpRequest)) {
      HttpEntity httpEntity = httpResponse.getEntity();
      String response = EntityUtils.toString(httpEntity);
      return JsonUtil.getInstance().toObject(response, responseClass);
    } catch (IOException | ParseException e) {
      log.error("Failed to make request: {}", httpRequest.getRequestUri(), e);
      throw new RuntimeException(e);
    }
  }
}
