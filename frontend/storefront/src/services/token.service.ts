import { getCookie, setCookie, clearAuthCookies } from '@/utils/cookies';
import { AUTH_STORAGE_KEYS } from '@/constants/auth.constants';
import { createLogger } from '@/utils/logger';

const logger = createLogger('TokenManager');

export interface TokenPair {
  accessToken: string;
  refreshToken: string;
}

class TokenManager {
  getAccessToken(): string | null {
    return getCookie(AUTH_STORAGE_KEYS.ACCESS_TOKEN);
  }

  getRefreshToken(): string | null {
    return getCookie(AUTH_STORAGE_KEYS.REFRESH_TOKEN);
  }

  setTokens(tokens: TokenPair): void {
    logger.info('Setting authentication tokens');
    setCookie(AUTH_STORAGE_KEYS.ACCESS_TOKEN, tokens.accessToken);
    setCookie(AUTH_STORAGE_KEYS.REFRESH_TOKEN, tokens.refreshToken);
  }

  clearTokens(): void {
    logger.info('Clearing authentication tokens');
    clearAuthCookies();
  }

  hasValidTokens(): boolean {
    const accessToken = this.getAccessToken();
    const refreshToken = this.getRefreshToken();
    return !!(accessToken && refreshToken);
  }
}

export const tokenManager = new TokenManager();
