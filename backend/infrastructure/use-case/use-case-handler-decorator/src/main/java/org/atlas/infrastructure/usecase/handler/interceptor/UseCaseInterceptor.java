package org.atlas.infrastructure.usecase.handler.interceptor;

public interface UseCaseInterceptor {

  void preHandle(Class<?> useCaseClass, Object input);

  void postHandle(Class<?> useCaseClass, Object input);

  void onError(Class<?> useCaseClass, Object input, Throwable e);
}
