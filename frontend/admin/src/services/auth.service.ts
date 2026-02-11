import type { User } from '@/interfaces/user.interface';
import type { LoginRequest, LoginResponse } from '@/interfaces/auth.interface';
import { iamAuthenticationApi, iamFrontApi } from '@/api/index.api';
import { AUTH_CONFIG } from '@/constants/auth.constants';
import { createLogger } from '@/utils/logger';

const logger = createLogger('AuthService');

export interface LoginResult {
  success: boolean;
  errorMessage?: string;
  userRole?: string;
  accessToken?: string;
  refreshToken?: string;
}

export interface ProfileResult {
  success: boolean;
  profile?: User;
  errorMessage?: string;
}

class AuthService {
  async login(credentials: LoginRequest): Promise<LoginResult> {
    try {
      logger.info('Login attempt', { username: credentials.username });
      
      const response = await iamAuthenticationApi.login(credentials);
      
      if (response.success && response.data) {
        logger.info('Login successful');
        return {
          success: true,
          accessToken: response.data.accessToken,
          refreshToken: response.data.refreshToken,
        };
      }
      
      logger.warn('Login failed', response.errorMessage);
      return {
        success: false,
        errorMessage: response.errorMessage || 'Login failed',
      };
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Login failed';
      logger.error('Login error', error);
      return { success: false, errorMessage };
    }
  }

  async fetchProfile(options?: { skipCache?: boolean }): Promise<ProfileResult> {
    try {
      logger.info('Fetching user profile', options);
      
      const response = await iamFrontApi.retrieveProfile();
      
      if (response.success && response.data) {
        logger.info('Profile fetched successfully', { email: response.data.email });
        return {
          success: true,
          profile: response.data,
        };
      }
      
      logger.error('Failed to fetch profile', response.errorMessage);
      return {
        success: false,
        errorMessage: response.errorMessage || 'Failed to load user profile',
      };
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to load user profile';
      logger.error('Profile fetch error', error);
      return { success: false, errorMessage };
    }
  }

  async logout(): Promise<void> {
    try {
      logger.info('Logout initiated');
      await iamAuthenticationApi.logout();
      logger.info('Logout completed');
    } catch (error) {
      logger.error('Logout error', error);
    }
  }

  isProfileCacheValid(lastFetched: number | null): boolean {
    if (!lastFetched) return false;
    
    const now = Date.now();
    const cacheAge = now - lastFetched;
    const isValid = cacheAge < AUTH_CONFIG.PROFILE_CACHE_TTL;
    
    logger.debug('Profile cache validation', { 
      lastFetched, 
      cacheAge, 
      isValid,
      ttl: AUTH_CONFIG.PROFILE_CACHE_TTL 
    });
    
    return isValid;
  }
}

export const authService = new AuthService();
