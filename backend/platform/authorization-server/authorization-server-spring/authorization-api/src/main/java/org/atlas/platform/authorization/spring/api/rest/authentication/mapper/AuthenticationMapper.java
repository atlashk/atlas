package org.atlas.platform.authorization.spring.api.rest.authentication.mapper;

import org.atlas.platform.authorization.spring.api.rest.authentication.model.GenerateOneTimeTokenRequest;
import org.atlas.platform.authorization.spring.api.rest.authentication.model.GenerateOneTimeTokenResponse;
import org.atlas.platform.authorization.spring.api.rest.authentication.model.LoginRequest;
import org.atlas.platform.authorization.spring.api.rest.authentication.model.LoginResponse;
import org.atlas.platform.authorization.spring.api.rest.authentication.model.OneTimeTokenLoginRequest;
import org.atlas.platform.authorization.spring.api.rest.authentication.model.RefreshTokenRequest;
import org.atlas.platform.authorization.spring.api.rest.authentication.model.RefreshTokenResponse;
import org.atlas.platform.authorization.port.in.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.platform.authorization.port.in.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.platform.authorization.port.in.authentication.model.LoginInput;
import org.atlas.platform.authorization.port.in.authentication.model.LoginOutput;
import org.atlas.platform.authorization.port.in.authentication.model.OneTimeTokenLoginInput;
import org.atlas.platform.authorization.port.in.authentication.model.RefreshTokenInput;
import org.atlas.platform.authorization.port.in.authentication.model.RefreshTokenOutput;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthenticationMapper {

  AuthenticationMapper INSTANCE = Mappers.getMapper(AuthenticationMapper.class);

  LoginInput toLoginInput(LoginRequest request);

  LoginResponse toLoginResponse(LoginOutput output);

  RefreshTokenInput toRefreshTokenInput(RefreshTokenRequest request);

  RefreshTokenResponse toRefreshTokenResponse(RefreshTokenOutput output);

  OneTimeTokenLoginInput toOneTimeTokenLoginInput(OneTimeTokenLoginRequest request);

  GenerateOneTimeTokenInput toGenerateOneTimeTokenInput(GenerateOneTimeTokenRequest request);

  GenerateOneTimeTokenResponse toGenerateOneTimeTokenResponse(GenerateOneTimeTokenOutput output);
}
