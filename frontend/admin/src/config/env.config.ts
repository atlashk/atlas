/**
 * Environment configuration
 * Centralized configuration for environment variables with fallback values
 */

export const ENV_CONFIG = {
  /**
   * API Base URL - defaults to localhost:8080 if not set
   */
  API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
  AUTHORIZATION_API_BASE_URL: process.env.NEXT_PUBLIC_AUTHORIZATION_API_BASE_URL || 'http://localhost:8081',
} as const;

// Export individual constants for convenience
export const { API_BASE_URL, AUTHORIZATION_API_BASE_URL } = ENV_CONFIG;
