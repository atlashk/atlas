package org.atlas.framework.function;

public interface Callback<R> {

  void onSuccess(R result);

  void onFailure(Throwable e);
}
