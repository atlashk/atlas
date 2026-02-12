import type { LoginRequest, LoginResponse, RegisterRequest, User } from "@/interfaces/iam.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class IamAuthenticationApi extends BaseApi {
  constructor() {
    super("/services/iam/api");
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
}

export class IamFrontApi extends BaseApi {
  constructor() {
    super("/services/iam/api");
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

export const iamAuthenticationApi = new IamAuthenticationApi();
export const iamFrontApi = new IamFrontApi();
