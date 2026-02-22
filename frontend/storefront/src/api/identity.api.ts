import type { LoginRequest, LoginResponse, RegisterRequest, User } from "@/interfaces/identity.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class IdentityApi extends BaseApi {
  constructor() {
    super("/services/identity/api");
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

  async retrieveProfile(): Promise<ApiResponse<User>> {
    return this.get<User>("/front/users/profile");
  }

  async register(userData: RegisterRequest): Promise<ApiResponse<void>> {
    return this.post<void>("/front/users/register", userData);
  }

  async changePassword(request: { oldPassword: string; newPassword: string }): Promise<ApiResponse<void>> {
    return this.post<void>("/front/users/change-password", request);
  }
}

export const identityApi = new IdentityApi();
