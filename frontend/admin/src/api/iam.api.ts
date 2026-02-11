import type { LoginRequest, LoginResponse } from "@/interfaces/auth.interface";
import type { ListUserFilters, RegisterRequest, User } from "@/interfaces/user.interface";
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

export class IamAdminApi extends BaseApi {
  constructor() {
    super("/services/iam/api");
  }

  async retrieveUserList(filters: ListUserFilters): Promise<ApiResponse<User[]>> {
    const payload: Record<string, unknown> = {
      page: filters.page || 1,
      size: filters.size || 20,
    };
    if (filters.id) payload.id = filters.id;
    if (filters.role) payload.role = filters.role;
    if (filters.keyword) {
      payload.username = filters.keyword;
    }
    return this.post<User[]>("/admin/users/list", payload);
  }

  async retrieveUser(id: string): Promise<ApiResponse<User>> {
    return this.get<User>(`/admin/users/${id}`);
  }

  async createUser(request: RegisterRequest & { role?: string }): Promise<ApiResponse<void>> {
    return this.post<void>("/admin/users", request);
  }

  async updateUser(id: string, request: Partial<RegisterRequest> & { role?: string; password?: string }): Promise<ApiResponse<void>> {
    return this.put<void>(`/admin/users/${id}`, request);
  }

  async deleteUser(id: string): Promise<ApiResponse<void>> {
    return this.delete<void>(`/admin/users/${id}`);
  }

  async countUser(): Promise<ApiResponse<number>> {
    return this.get<number>("/admin/users/statistics/count");
  }
}

export const iamAuthenticationApi = new IamAuthenticationApi();
export const iamFrontApi = new IamFrontApi();
export const iamAdminApi = new IamAdminApi();
