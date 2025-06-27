import type { ApiResponse } from '@/interfaces/api.interface'
import type { ListUserFilters, RegisterRequest, User } from '@/interfaces/user.interface'
import { BaseService } from './base.service'

export class UserService extends BaseService {
  constructor() {
    super('/api')
  }

  // Common operations
  async getProfile(): Promise<ApiResponse<User>> {
    return this.get<User>('/common/users/profile')
  }

  // Front operations
  async register(userData: RegisterRequest): Promise<ApiResponse<void>> {
    return this.post<void>('/front/users/register', userData)
  }

  // Admin operations
  async listUser(filters: ListUserFilters): Promise<ApiResponse<User[]>> {
    const queryParams = new URLSearchParams()
    if (filters.id) queryParams.append('id', filters.id)
    if (filters.username) queryParams.append('username', filters.username)
    if (filters.role) queryParams.append('role', filters.role)
    queryParams.append('page', filters.page.toString())
    queryParams.append('size', filters.size.toString())

    return this.get<User[]>(`/admin/users?${queryParams.toString()}`)
  }
}

export const userService = new UserService() 