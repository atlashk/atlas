package org.atlas.common.framework.qrcode;

public interface QRCodeGenerator {

  byte[] generate(String text) throws Exception;

  byte[] generate(String text, int width, int height) throws Exception;
}
