package org.atlas.services.iam.port.in.authentication.service;

public interface AuthenticationAdminService {

  void resetPassword(String userId) throws Exception;
}
