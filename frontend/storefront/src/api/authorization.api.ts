import type { ChangePasswordRequest, LoginRequest, LoginResponse, RegisterRequest, User } from "@/interfaces/user.interface";
import { AUTHORIZATION_API_BASE_URL } from "@/config/env.config";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class AuthorizationApi extends BaseApi {
  constructor() {
    super(`${AUTHORIZATION_API_BASE_URL}/api`);
  }

  async login(request: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return this.post<LoginResponse>("/authentication/login", request);
  }

  async refreshToken(refreshToken: string): Promise<ApiResponse<LoginResponse>> {
    return this.post<LoginResponse>("/authentication/refresh-token", { refreshToken });
  }

  async logout(): Promise<ApiResponse<void>> {
    return this.post<void>("/authentication/logout");
  }

  async changePassword(request: ChangePasswordRequest): Promise<ApiResponse<void>> {
    return this.post<void>("/authentication/change-password", request);
  }

  async retrieveProfile(): Promise<ApiResponse<User>> {
    return this.get<User>("/users/profile");
  }

  async register(userData: RegisterRequest): Promise<ApiResponse<void>> {
    return this.post<void>("/users/register", userData);
  }
}

export const authorizationApi = new AuthorizationApi();
