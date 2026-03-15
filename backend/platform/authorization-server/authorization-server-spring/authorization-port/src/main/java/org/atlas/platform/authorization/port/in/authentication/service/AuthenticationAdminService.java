package org.atlas.platform.authorization.port.in.authentication.service;

public interface AuthenticationAdminService {

  void resetPassword(String userId) throws Exception;
}
