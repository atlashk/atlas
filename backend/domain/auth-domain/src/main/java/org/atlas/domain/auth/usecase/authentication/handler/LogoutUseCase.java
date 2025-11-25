package org.atlas.domain.auth.usecase.authentication.handler;

public interface LogoutUseCase {

  void handle(String accessToken) throws Exception;
}
