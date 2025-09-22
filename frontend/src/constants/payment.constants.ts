// Payment method constants
export const PAYMENT_METHODS = [
  'stripe',
  'qr_code',
  'redirect',
  'deep_link'
] as const;

export type PaymentMethod = typeof PAYMENT_METHODS[number];

// Payment status constants
export const PAYMENT_STATUSES = [
  'idle',
  'pending',
  'processing',
  'requires_action',
  'succeeded',
  'failed',
  'canceled'
] as const;

export type PaymentStatus = typeof PAYMENT_STATUSES[number];

// Payment currencies
export const PAYMENT_CURRENCIES = [
  'USD',
  'EUR',
  'GBP',
  'VND',
  'JPY'
] as const;

export type PaymentCurrency = typeof PAYMENT_CURRENCIES[number];

// Payment next action types
export const PAYMENT_NEXT_ACTION_TYPES = [
  'use_payment_element',
  'redirect_url',
  'deeplink',
  'qr_code'
] as const;

export type PaymentNextActionType = typeof PAYMENT_NEXT_ACTION_TYPES[number];

// Payment error types
export const PAYMENT_ERROR_TYPES = [
  'card_error',
  'validation_error',
  'api_error',
  'rate_limit_error',
  'authentication_error'
] as const;

export type PaymentErrorType = typeof PAYMENT_ERROR_TYPES[number];

// Loading states
export const LOADING_STATES = [
  'idle',
  'loading',
  'success',
  'error'
] as const;

export type LoadingState = typeof LOADING_STATES[number];

// Payment configuration constants
export const PAYMENT_CONFIG = {
  // Default values
  DEFAULT_CURRENCY: 'USD' as PaymentCurrency,
  DEFAULT_TIMEOUT: 30000, // 30 seconds
  DEFAULT_RETRY_ATTEMPTS: 3,
  
  // Amount limits (in cents for USD)
  MIN_AMOUNT: 50, // $0.50
  MAX_AMOUNT: 99999999, // $999,999.99
  
  // Polling configuration
  POLLING_INTERVAL: 2000, // 2 seconds
  MAX_POLLING_ATTEMPTS: 30, // 1 minute total
  
  // Stripe configuration
  STRIPE: {
    API_VERSION: '2023-10-16',
    SUPPORTED_LOCALES: ['en', 'vi', 'ja', 'fr', 'de', 'es'],
    DEFAULT_LOCALE: 'en',
  },
  
  // QR Code configuration
  QR_CODE: {
    SIZE: 256,
    ERROR_CORRECTION_LEVEL: 'M',
    MARGIN: 4,
  },
  
  // Redirect configuration
  REDIRECT: {
    TIMEOUT: 300000, // 5 minutes
  },
  
  // Deep link configuration
  DEEP_LINK: {
    TIMEOUT: 60000, // 1 minute
    FALLBACK_DELAY: 3000, // 3 seconds
  },
} as const;

// Payment method display names
export const PAYMENT_METHOD_NAMES: Record<PaymentMethod, string> = {
  stripe: 'Credit/Debit Card',
  qr_code: 'QR Code Payment',
  redirect: 'Bank Transfer',
  deep_link: 'Mobile App Payment',
} as const;

// Payment status display names
export const PAYMENT_STATUS_NAMES: Record<PaymentStatus, string> = {
  idle: 'Not Started',
  pending: 'Pending',
  processing: 'Processing',
  requires_action: 'Action Required',
  succeeded: 'Completed',
  failed: 'Failed',
  canceled: 'Canceled',
} as const;

// Currency display information
export const CURRENCY_INFO: Record<PaymentCurrency, {
  symbol: string;
  name: string;
  decimals: number;
  code: string;
}> = {
  USD: {
    symbol: '$',
    name: 'US Dollar',
    decimals: 2,
    code: 'USD',
  },
  EUR: {
    symbol: '€',
    name: 'Euro',
    decimals: 2,
    code: 'EUR',
  },
  GBP: {
    symbol: '£',
    name: 'British Pound',
    decimals: 2,
    code: 'GBP',
  },
  VND: {
    symbol: '₫',
    name: 'Vietnamese Dong',
    decimals: 0,
    code: 'VND',
  },
  JPY: {
    symbol: '¥',
    name: 'Japanese Yen',
    decimals: 0,
    code: 'JPY',
  },
} as const;

// Payment error codes
export const PAYMENT_ERROR_CODES = {
  // Generic errors
  UNKNOWN_ERROR: 'UNKNOWN_ERROR',
  NETWORK_ERROR: 'NETWORK_ERROR',
  TIMEOUT_ERROR: 'TIMEOUT_ERROR',
  
  // Validation errors
  INVALID_PAYMENT_METHOD: 'INVALID_PAYMENT_METHOD',
  INVALID_AMOUNT: 'INVALID_AMOUNT',
  INVALID_CURRENCY: 'INVALID_CURRENCY',
  MISSING_REQUIRED_FIELD: 'MISSING_REQUIRED_FIELD',
  
  // Payment processing errors
  PAYMENT_DECLINED: 'PAYMENT_DECLINED',
  INSUFFICIENT_FUNDS: 'INSUFFICIENT_FUNDS',
  CARD_EXPIRED: 'CARD_EXPIRED',
  INVALID_CARD: 'INVALID_CARD',
  PROCESSING_ERROR: 'PROCESSING_ERROR',
  
  // Authentication errors
  AUTHENTICATION_FAILED: 'AUTHENTICATION_FAILED',
  AUTHORIZATION_FAILED: 'AUTHORIZATION_FAILED',
  
  // Service errors
  SERVICE_UNAVAILABLE: 'SERVICE_UNAVAILABLE',
  RATE_LIMIT_EXCEEDED: 'RATE_LIMIT_EXCEEDED',
  CONFIGURATION_ERROR: 'CONFIGURATION_ERROR',
  
  // Stripe specific errors
  STRIPE_INITIALIZATION_FAILED: 'STRIPE_INITIALIZATION_FAILED',
  STRIPE_ELEMENTS_NOT_READY: 'STRIPE_ELEMENTS_NOT_READY',
  STRIPE_PAYMENT_FAILED: 'STRIPE_PAYMENT_FAILED',
} as const;

export type PaymentErrorCode = typeof PAYMENT_ERROR_CODES[keyof typeof PAYMENT_ERROR_CODES];

// Payment loading operation IDs
export const PAYMENT_LOADING_OPERATIONS = {
  INITIALIZE_PAYMENT: 'payment.initialize',
  PROCESS_PAYMENT: 'payment.process',
  CONFIRM_PAYMENT: 'payment.confirm',
  CREATE_PAYMENT_METHOD: 'payment.create_method',
  VALIDATE_PAYMENT: 'payment.validate',
  FETCH_PAYMENT_STATUS: 'payment.fetch_status',
  STRIPE_INITIALIZE: 'stripe.initialize',
  STRIPE_LOAD_ELEMENTS: 'stripe.load_elements',
} as const;

export type PaymentLoadingOperation = typeof PAYMENT_LOADING_OPERATIONS[keyof typeof PAYMENT_LOADING_OPERATIONS];

// Payment loading messages
export const PAYMENT_LOADING_MESSAGES = {
  INITIALIZING: 'Initializing payment...',
  PROCESSING: 'Processing payment...',
  CONFIRMING: 'Confirming payment...',
  VALIDATING: 'Validating payment information...',
  CREATING_METHOD: 'Creating payment method...',
  FETCHING_STATUS: 'Checking payment status...',
  STRIPE_LOADING: 'Loading Stripe...',
  STRIPE_ELEMENTS: 'Preparing payment form...',
  COMPLETING: 'Completing payment...',
  SUCCESS: 'Payment completed successfully!',
  FAILED: 'Payment failed',
} as const;

// API endpoints
export const PAYMENT_API_ENDPOINTS = {
  CREATE_PAYMENT: '/api/payments',
  GET_PAYMENT: '/api/payments/:id',
  UPDATE_PAYMENT: '/api/payments/:id',
  CANCEL_PAYMENT: '/api/payments/:id/cancel',
  GET_PAYMENT_STATUS: '/api/payments/:id/status',
  GET_PAYMENT_METHODS: '/api/payment-methods',
  CREATE_PAYMENT_INTENT: '/api/payments/intent',
  CONFIRM_PAYMENT_INTENT: '/api/payments/intent/:id/confirm',
} as const;

// Event names for payment tracking
export const PAYMENT_EVENTS = {
  PAYMENT_STARTED: 'payment_started',
  PAYMENT_METHOD_SELECTED: 'payment_method_selected',
  PAYMENT_FORM_SUBMITTED: 'payment_form_submitted',
  PAYMENT_PROCESSING: 'payment_processing',
  PAYMENT_SUCCEEDED: 'payment_succeeded',
  PAYMENT_FAILED: 'payment_failed',
  PAYMENT_CANCELED: 'payment_canceled',
  PAYMENT_REQUIRES_ACTION: 'payment_requires_action',
  PAYMENT_ACTION_COMPLETED: 'payment_action_completed',
} as const;

export type PaymentEvent = typeof PAYMENT_EVENTS[keyof typeof PAYMENT_EVENTS];

// CSS class names for consistent styling
export const PAYMENT_CSS_CLASSES = {
  CONTAINER: 'payment-container',
  FORM: 'payment-form',
  METHOD_SELECTOR: 'payment-method-selector',
  METHOD_OPTION: 'payment-method-option',
  FORM_FIELD: 'payment-form-field',
  ERROR_MESSAGE: 'payment-error-message',
  SUCCESS_MESSAGE: 'payment-success-message',
  LOADING_SPINNER: 'payment-loading-spinner',
  SUBMIT_BUTTON: 'payment-submit-button',
  CANCEL_BUTTON: 'payment-cancel-button',
  STATUS_MODAL: 'payment-status-modal',
  QR_CODE: 'payment-qr-code',
  REDIRECT_MESSAGE: 'payment-redirect-message',
} as const;

// Validation rules
export const PAYMENT_VALIDATION_RULES = {
  CARD_NUMBER: {
    MIN_LENGTH: 13,
    MAX_LENGTH: 19,
    PATTERN: /^\d{13,19}$/,
  },
  CVV: {
    MIN_LENGTH: 3,
    MAX_LENGTH: 4,
    PATTERN: /^\d{3,4}$/,
  },
  EXPIRY_DATE: {
    PATTERN: /^(0[1-9]|1[0-2])\/\d{2}$/,
  },
  EMAIL: {
    PATTERN: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  },
  AMOUNT: {
    MIN: PAYMENT_CONFIG.MIN_AMOUNT,
    MAX: PAYMENT_CONFIG.MAX_AMOUNT,
  },
} as const;