import type { ApiResponse } from '@/interfaces/api.interface'
import type { LoginRequest, LoginResponse } from '@/interfaces/auth.interface'
import { BaseService } from './base.service'

export class AuthService extends BaseService {
  constructor() {
    super('/api/auth')
  }

  async login(credentials: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return this.post<LoginResponse>('/login', credentials)
  }

  async refreshToken(refreshToken: string): Promise<ApiResponse<LoginResponse>> {
    return this.post<LoginResponse>('/refresh-token', { refreshToken })
  }

  async logout(): Promise<ApiResponse<void>> {
    return this.post<void>('/logout')
  }
}

export const authService = new AuthService()
