import { AUTHORIZATION_API_BASE_URL } from "@/config/env.config";
import type { ChangePasswordRequest, LoginRequest, LoginResponse } from "@/interfaces/authorization.interface";
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

  async resetUserPassword(userId: string): Promise<ApiResponse<void>> {
    return this.post<void>("/authentication/admin/reset-password", { userId });
  }
}

export const authorizationApi = new AuthorizationApi();
