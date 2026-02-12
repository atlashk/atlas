import type { ListUserFilters, LoginRequest, LoginResponse, RegisterRequest, User } from "@/interfaces/iam.interface";
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

export class IamAdminApi extends BaseApi {
  constructor() {
    super("/services/iam/api/admin");
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
    return this.post<User[]>("/users/list", payload);
  }

  async retrieveUser(id: string): Promise<ApiResponse<User>> {
    return this.get<User>(`/users/${id}`);
  }

  async createUser(request: RegisterRequest & { role?: string }): Promise<ApiResponse<void>> {
    return this.post<void>("/users", request);
  }

  async updateUser(id: string, request: Partial<RegisterRequest> & { role?: string; password?: string }): Promise<ApiResponse<void>> {
    return this.put<void>(`/users/${id}`, request);
  }

  async deleteUser(id: string): Promise<ApiResponse<void>> {
    return this.delete<void>(`/users/${id}`);
  }

  async countUser(): Promise<ApiResponse<number>> {
    return this.get<number>("/users/statistics/count");
  }
}

export const iamAuthenticationApi = new IamAuthenticationApi();
export const iamAdminApi = new IamAdminApi();
