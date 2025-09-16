package org.atlas.infrastructure.api.server.rest.core.logging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CachedServletInputStream extends ServletInputStream {

  private final InputStream cachedInputStream;

  public CachedServletInputStream(byte[] cachedPayload) {
    this.cachedInputStream = new ByteArrayInputStream(cachedPayload);
  }

  @Override
  public boolean isFinished() {
    try {
      return cachedInputStream.available() == 0;
    } catch (IOException e) {
      log.error("Cached input stream cannot be available ", e);
    }
    return false;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int read() throws IOException {
    return cachedInputStream.read();
  }
}