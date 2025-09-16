package org.atlas.framework.concurrent;

public interface AsyncTask extends Runnable {

  void onSuccess();

  void onError(Throwable ex);
}
