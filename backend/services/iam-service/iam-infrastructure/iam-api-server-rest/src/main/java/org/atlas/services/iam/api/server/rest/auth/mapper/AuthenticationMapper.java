package org.atlas.services.iam.api.server.rest.auth.mapper;

import org.atlas.services.iam.api.server.rest.auth.model.GenerateOneTimeTokenRequest;
import org.atlas.services.iam.api.server.rest.auth.model.GenerateOneTimeTokenResponse;
import org.atlas.services.iam.api.server.rest.auth.model.LoginRequest;
import org.atlas.services.iam.api.server.rest.auth.model.LoginResponse;
import org.atlas.services.iam.api.server.rest.auth.model.OneTimeTokenLoginRequest;
import org.atlas.services.iam.api.server.rest.auth.model.RefreshTokenRequest;
import org.atlas.services.iam.api.server.rest.auth.model.RefreshTokenResponse;
import org.atlas.services.iam.port.in.auth.model.GenerateOneTimeTokenInput;
import org.atlas.services.iam.port.in.auth.model.GenerateOneTimeTokenOutput;
import org.atlas.services.iam.port.in.auth.model.LoginInput;
import org.atlas.services.iam.port.in.auth.model.LoginOutput;
import org.atlas.services.iam.port.in.auth.model.OneTimeTokenLoginInput;
import org.atlas.services.iam.port.in.auth.model.RefreshTokenInput;
import org.atlas.services.iam.port.in.auth.model.RefreshTokenOutput;
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
