import type { ChangePasswordRequest, ListUserFilters, LoginRequest, LoginResponse, RegisterRequest, User } from "@/interfaces/identity.interface";
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

  async changePassword(request: ChangePasswordRequest): Promise<ApiResponse<void>> {
    return this.post<void>("/authentication/change-password", request);
  }

 async retrieveProfile(): Promise<ApiResponse<User>> {
    return this.get<User>("/users/profile");
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
    return this.post<User[]>("/users/admin/list", payload);
  }

  async retrieveReferenceData(type: string): Promise<ApiResponse<Record<string, string>>> {
    return this.get<Record<string, string>>(`/reference-data?type=${type}`);
  }

  async retrieveUserRoles(): Promise<ApiResponse<Record<string, string>>> {
    return this.retrieveReferenceData('USER_ROLE');
  }

  async retrieveUser(id: string): Promise<ApiResponse<User>> {
    return this.get<User>(`/users/admin/${id}`);
  }

  async createUser(request: RegisterRequest & { role?: string }): Promise<ApiResponse<void>> {
    return this.post<void>("/users/admin", request);
  }

  async updateUser(id: string, request: Partial<RegisterRequest> & { role?: string; password?: string }): Promise<ApiResponse<void>> {
    return this.put<void>(`/users/admin/${id}`, request);
  }

  async deleteUser(id: string): Promise<ApiResponse<void>> {
    return this.delete<void>(`/users/admin/${id}`);
  }

  async resetUserPassword(userId: string): Promise<ApiResponse<void>> {
    return this.post<void>("/authentication/admin/reset-password", { userId });
  }

  async retrieveTotalUserCount(): Promise<ApiResponse<number>> {
    return this.get<number>("/users/admin/statistics/count");
  }
}

export const identityApi = new IdentityApi();
