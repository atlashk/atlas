import { RegisterRequest, User } from "@/interfaces";
import { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";

export class UserApi extends BaseApi {
  constructor() {
    super("/services/user/api");
  }

  async retrieveProfile(): Promise<ApiResponse<User>> {
    return this.get<User>("/users/profile");
  }

  async register(userData: RegisterRequest): Promise<ApiResponse<void>> {
    return this.post<void>("/users/register", userData);
  }
}

export const userApi = new UserApi();
