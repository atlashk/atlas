import type { LoginRequest, LoginResponse } from "@/interfaces/auth.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class AuthApi extends BaseApi {
  constructor() {
    super("/api/authentication");
  }

  async login(request: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return this.post<LoginResponse>("/login", request);
  }

  async refreshToken(
    refreshToken: string
  ): Promise<ApiResponse<LoginResponse>> {
    return this.post<LoginResponse>("/refresh-token", { refreshToken });
  }

  async logout(): Promise<ApiResponse<void>> {
    return this.post<void>("/logout");
  }
}

export const authApi = new AuthApi();
