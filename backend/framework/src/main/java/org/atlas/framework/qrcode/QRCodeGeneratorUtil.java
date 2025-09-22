package org.atlas.framework.qrcode;

import org.atlas.framework.qrcode.zxing.ZxingAdapter;

/**
 * Implement Singleton pattern with Bill Pugh solution
 */
public class QRCodeGeneratorUtil {

  public static QRCodeGenerator getInstance() {
    return JsonHolder.INSTANCE;
  }

  private static class JsonHolder {

    private static final QRCodeGenerator INSTANCE = new ZxingAdapter();
  }
}
