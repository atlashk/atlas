package org.atlas.framework.template.exception;

public class ResolveTemplateException extends RuntimeException {

  public ResolveTemplateException(String message) {
    super(message);
  }

  public ResolveTemplateException(String message, Throwable cause) {
    super(message, cause);
  }
}
