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
  IDP: process.env.NEXT_PUBLIC_IDP || 'keycloak',
  KEYCLOAK_URL: process.env.NEXT_PUBLIC_KEYCLOAK_URL || process.env.NEXT_PUBLIC_AUTHORIZATION_API_BASE_URL || 'http://localhost:8901',
  KEYCLOAK_REALM: process.env.NEXT_PUBLIC_KEYCLOAK_REALM || 'master',
  KEYCLOAK_CLIENT_ID: process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID || 'web-client',

  /**
   * Stripe Publishable Key
   */
  STRIPE_PUBLISHABLE_KEY: process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY || '',
} as const;

// Export individual constants for convenience
export const {
  API_BASE_URL,
  AUTHORIZATION_API_BASE_URL,
  IDP,
  KEYCLOAK_URL,
  KEYCLOAK_REALM,
  KEYCLOAK_CLIENT_ID,
  STRIPE_PUBLISHABLE_KEY
} = ENV_CONFIG;
