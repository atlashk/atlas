import { jwtDecode } from 'jwt-decode';
import type { JWTPayload } from '@/interfaces/auth.interface';

// Token utilities
export const getAccessToken = (): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('accessToken');
};

export const getRefreshToken = (): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('refreshToken');
};

export const setTokens = (accessToken: string, refreshToken: string): void => {
  if (typeof window === 'undefined') return;
  
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
};

export const clearTokens = (): void => {
  if (typeof window === 'undefined') return;
  
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  sessionStorage.removeItem('user');
  sessionStorage.removeItem('authState');
};

// Token validation
export const isValidToken = (token: string): boolean => {
  try {
    if (!token) return false;
    
    const decoded = jwtDecode<JWTPayload>(token);
    const currentTime = Math.floor(Date.now() / 1000);
    
    return decoded.exp > currentTime;
  } catch {
    return false;
  }
};

export const isTokenExpiringSoon = (token: string, thresholdSeconds: number = 300): boolean => {
  try {
    if (!token) return true;
    
    const decoded = jwtDecode<JWTPayload>(token);
    const currentTime = Math.floor(Date.now() / 1000);
    const timeUntilExpiry = decoded.exp - currentTime;
    
    return timeUntilExpiry <= thresholdSeconds;
  } catch {
    return true;
  }
};

export const getTokenPayload = (token: string): JWTPayload | null => {
  try {
    if (!token) return null;
    return jwtDecode<JWTPayload>(token);
  } catch {
    return null;
  }
};

// Auth error handling
export const getErrorMessage = (error: unknown): string => {
  if (error instanceof Error) {
    return error.message;
  }
  
  if (typeof error === 'object' && error !== null && 'message' in error) {
    return String(error.message);
  }
  
  return 'An unknown error occurred';
};

// Simple logout confirmation
export const confirmLogout = async (message: string = 'Are you sure you want to log out?'): Promise<boolean> => {
  if (typeof window === 'undefined') return true;
  return window.confirm(message);
};