import type { ListUserFilters, User } from "@/interfaces/user.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class UserAdminApi extends BaseApi {
  constructor() {
    super("/api/user-svc/admin");
  }

  async listUser(filters: ListUserFilters): Promise<ApiResponse<User[]>> {
    const queryParams = new URLSearchParams();
    if (filters.id) {
      queryParams.append("id", filters.id);
    }
    if (filters.keyword) {
      queryParams.append("keyword", filters.keyword);
    }
    if (filters.role) {
      queryParams.append("role", filters.role);
    }
    queryParams.append("page", (filters.page || 1).toString());
    queryParams.append("size", (filters.size || 20).toString());

    return this.get<User[]>(`/users?${queryParams.toString()}`);
  }

  async countUser(): Promise<ApiResponse<number>> {
    return this.get<number>("/users/statistics/count");
  }
}

export const userAdminApi = new UserAdminApi();
