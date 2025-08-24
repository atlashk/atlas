package org.atlas.framework.qrgenerator;

import org.atlas.framework.qrgenerator.zxing.ZxingAdapter;

/**
 * Implement Singleton pattern with Bill Pugh solution
 */
public class QrGeneratorUtil {

  public static QrGenerator getInstance() {
    return JsonHolder.INSTANCE;
  }

  private static class JsonHolder {

    private static final QrGenerator INSTANCE = new ZxingAdapter();
  }
}
