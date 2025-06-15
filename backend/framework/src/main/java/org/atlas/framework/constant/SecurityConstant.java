package org.atlas.framework.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstant {

  // Relative path to resources folder
  public static final String RSA_PUBLIC_KEY_PATH = "secret/default.pub";
  public static final String RSA_PRIVATE_KEY_PATH = "secret/default.key";

  // Token
  public static final String TOKEN_ISSUER = "atlas";
  public static final String TOKEN_AUDIENCE = "atlas";
  public static final String JWKS_KEY_ID = "atlas";
  public static final long ACCESS_TOKEN_EXPIRATION_TIME = 24 * 60 * 60; // 1 day in seconds
  public static final long REFRESH_TOKEN_EXPIRATION_TIME = 30L * 24 * 60 * 60; // 30 days in seconds
  public static final String SESSION_BLACKLISTED_REDIS_KEY_FORMAT = "session:%s:blacklisted";
  public static final String LAST_LOGOUT_TS_REDIS_KEY_FORMAT = "user:%s:last_logout_ts";

  // Cookie
  public static final String ACCESS_TOKEN_COOKIE = "accessToken";
  public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
}
