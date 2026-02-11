/**
 * Environment configuration
 * Centralized configuration for environment variables with fallback values
 */

export const ENV_CONFIG = {
  /**
   * API Base URL - defaults to localhost:8080 if not set
   */
  API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
  
  /**
   * Stripe Publishable Key
   */
  STRIPE_PUBLISHABLE_KEY: process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY || '',
} as const;

// Export individual constants for convenience
export const { API_BASE_URL, STRIPE_PUBLISHABLE_KEY } = ENV_CONFIG;
