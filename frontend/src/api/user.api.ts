import type { RegisterRequest, User } from "@/interfaces/user.interface";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class UserApi extends BaseApi {
  constructor() {
    super("/services/user/api");
  }

  async getProfile(): Promise<ApiResponse<User>> {
    return this.get<User>("/users/profile");
  }

  async register(userData: RegisterRequest): Promise<ApiResponse<void>> {
    return this.post<void>("/users/register", userData);
  }
}

export const userApi = new UserApi();
