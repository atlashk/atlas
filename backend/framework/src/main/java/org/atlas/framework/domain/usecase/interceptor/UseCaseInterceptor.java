package org.atlas.framework.domain.usecase.interceptor;

public interface UseCaseInterceptor {

  void preHandle(Class<?> useCaseClass, Object input);

  void postHandle(Class<?> useCaseClass, Object input);

  void onError(Class<?> useCaseClass, Object input, Throwable e);
}
