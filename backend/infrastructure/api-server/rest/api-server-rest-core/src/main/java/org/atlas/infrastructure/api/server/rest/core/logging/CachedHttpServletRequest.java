package org.atlas.infrastructure.api.server.rest.core.logging;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.util.StreamUtils;

/**
 * We read the actual request body inside the wrapper constructor and store it in the cachedPayload
 * byte array
 */
public class CachedHttpServletRequest extends HttpServletRequestWrapper {

  private final byte[] cachedPayload;

  public CachedHttpServletRequest(HttpServletRequest request) throws IOException {
    super(request);
    InputStream requestInputStream = request.getInputStream();
    this.cachedPayload = StreamUtils.copyToByteArray(requestInputStream);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new CachedServletInputStream(this.cachedPayload);
  }

  @Override
  public BufferedReader getReader() throws IOException {
    // Create a reader from cachedContent
    // and return it
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedPayload);
    return new BufferedReader(new InputStreamReader(byteArrayInputStream));
  }
}