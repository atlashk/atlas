package org.atlas.libs.framework.security;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OAuth2Constant {

  // Grant types
  public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
  public static final String GRANT_TYPE_PASSWORD = "password";
  public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
  public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
}
