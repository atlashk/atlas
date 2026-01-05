package org.atlas.common.framework.qrcode;

import org.atlas.common.framework.qrcode.zxing.ZxingQRCodeGenerator;

/**
 * Implement Singleton pattern with Bill Pugh solution
 */
public class QRCodeGeneratorUtil {

  public static QRCodeGenerator getInstance() {
    return JsonHolder.INSTANCE;
  }

  private static class JsonHolder {

    private static final QRCodeGenerator INSTANCE = new ZxingQRCodeGenerator();
  }
}
