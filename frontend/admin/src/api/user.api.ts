import type { CreateUserRequest, RetrieveUserListFilter, UpdateUserRequest, User } from "@/interfaces/user.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class UserApi extends BaseApi {
  constructor() {
    super("/services/user/api");
  }

 async retrieveProfile(): Promise<ApiResponse<User>> {
    return this.get<User>("/users/profile");
  }

  async retrieveUserList(filters: RetrieveUserListFilter): Promise<ApiResponse<User[]>> {
    const payload: Record<string, unknown> = {
      page: filters.page || 1,
      size: filters.size || 20,
    };
    if (filters.id) payload.id = filters.id;
    if (filters.role) payload.role = filters.role;
    if (filters.firstName) payload.firstName = filters.firstName;
    if (filters.lastName) payload.lastName = filters.lastName;
    if (filters.email) payload.email = filters.email;
    if (filters.phone) payload.phone = filters.phone;
    return this.post<User[]>("/users/admin/list", payload);
  }

  async retrieveReferenceData(type: string): Promise<ApiResponse<Record<string, string>>> {
    return this.get<Record<string, string>>(`/public/reference-data?type=${type}`);
  }

  async retrieveUserRoles(): Promise<ApiResponse<Record<string, string>>> {
    return this.retrieveReferenceData('USER_ROLE');
  }

  async retrieveUser(id: string): Promise<ApiResponse<User>> {
    return this.get<User>(`/users/admin/${id}`);
  }

  async createUser(request: CreateUserRequest): Promise<ApiResponse<void>> {
    return this.post<void>("/users/admin", request);
  }

  async updateUser(id: string, request: UpdateUserRequest): Promise<ApiResponse<void>> {
    return this.put<void>(`/users/admin/${id}`, request);
  }

  async deleteUser(id: string): Promise<ApiResponse<void>> {
    return this.delete<void>(`/users/admin/${id}`);
  }

  async retrieveTotalUserCount(): Promise<ApiResponse<number>> {
    return this.get<number>("/users/admin/statistics/count");
  }
}

export const userApi = new UserApi();
