package org.atlas.services.identity.port.in.authentication.service;

public interface AuthenticationAdminService {

  void resetPassword(String userId) throws Exception;
}
