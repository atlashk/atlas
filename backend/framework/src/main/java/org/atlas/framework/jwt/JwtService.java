package org.atlas.framework.jwt;

public interface JwtService {

  String JWT_PREFIX = "Bearer ";

  String encodeJwt(EncodeJwtInput encodeJwtInput);

  Jwt decodeJwt(DecodeJwtInput input) throws InvalidJwtException;
}
