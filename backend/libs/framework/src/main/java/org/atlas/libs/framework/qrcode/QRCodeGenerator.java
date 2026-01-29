package org.atlas.libs.framework.qrcode;

public interface QRCodeGenerator {

  byte[] generate(String text) throws Exception;

  byte[] generate(String text, int width, int height) throws Exception;
}
