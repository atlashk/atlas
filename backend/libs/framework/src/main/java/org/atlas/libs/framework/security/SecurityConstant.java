package org.atlas.libs.framework.security;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstant {

  // Relative path to resources folder
  public static final String RSA_PUBLIC_KEY_PATH = "secret/token.pub";
  public static final String RSA_PRIVATE_KEY_PATH = "secret/token.key";

  // Token
  public static final String TOKEN_ISSUER = "atlas";
  public static final String TOKEN_AUDIENCE = "atlas";
  public static final String JWKS_KEY_ID = "atlas";
  public static final long ACCESS_TOKEN_EXPIRATION_TIME = 24 * 60 * 60; // 1 day in seconds
  public static final long REFRESH_TOKEN_EXPIRATION_TIME = 30L * 24 * 60 * 60; // 30 days in seconds
  public static final String TOKEN_BLACKLISTED_KV_STORE_NAME = "token_blacklisted";
}
