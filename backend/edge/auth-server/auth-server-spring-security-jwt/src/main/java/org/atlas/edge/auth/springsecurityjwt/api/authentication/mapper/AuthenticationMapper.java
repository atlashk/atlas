package org.atlas.edge.auth.springsecurityjwt.api.authentication.mapper;

import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.domain.auth.usecase.authentication.model.LoginInput;
import org.atlas.domain.auth.usecase.authentication.model.LoginOutput;
import org.atlas.domain.auth.usecase.authentication.model.OneTimeTokenLoginInput;
import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenOutput;
import org.atlas.edge.auth.springsecurityjwt.api.authentication.model.GenerateOneTimeTokenRequest;
import org.atlas.edge.auth.springsecurityjwt.api.authentication.model.GenerateOneTimeTokenResponse;
import org.atlas.edge.auth.springsecurityjwt.api.authentication.model.LoginRequest;
import org.atlas.edge.auth.springsecurityjwt.api.authentication.model.LoginResponse;
import org.atlas.edge.auth.springsecurityjwt.api.authentication.model.OneTimeTokenLoginRequest;
import org.atlas.edge.auth.springsecurityjwt.api.authentication.model.RefreshTokenRequest;
import org.atlas.edge.auth.springsecurityjwt.api.authentication.model.RefreshTokenResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthenticationMapper {

  AuthenticationMapper INSTANCE = Mappers.getMapper(AuthenticationMapper.class);

  LoginInput toLoginInput(LoginRequest request);

  LoginResponse toLoginResponse(LoginOutput output);

  OneTimeTokenLoginInput toOneTimeTokenLoginInput(OneTimeTokenLoginRequest request);

  GenerateOneTimeTokenInput toGenerateOneTimeTokenInput(GenerateOneTimeTokenRequest request);

  GenerateOneTimeTokenResponse toGenerateOneTimeTokenResponse(GenerateOneTimeTokenOutput output);

  RefreshTokenInput toRefreshTokenInput(RefreshTokenRequest request);

  RefreshTokenResponse toRefreshTokenResponse(RefreshTokenOutput output);
}
