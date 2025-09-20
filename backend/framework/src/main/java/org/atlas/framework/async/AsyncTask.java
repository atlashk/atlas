package org.atlas.framework.async;

public interface AsyncTask extends Runnable {

  void onSuccess();

  void onError(Throwable ex);
}
