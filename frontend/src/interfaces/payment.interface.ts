// Core payment types
export type PaymentMethod = 'stripe' | 'qr_code' | 'redirect' | 'deep_link';
export type PaymentStatus = 'idle' | 'pending' | 'processing' | 'requires_action' | 'succeeded' | 'failed' | 'canceled';
export type PaymentCurrency = 'USD' | 'EUR' | 'GBP' | 'VND' | 'JPY';
export type PaymentNextActionType = 'use_payment_element' | 'redirect_url' | 'deeplink' | 'qr_code';

// Enhanced payment next action interface
export interface PaymentNextAction {
  type: PaymentNextActionType;
  provider?: string;
  client_secret?: string;
  publishable_key?: string;
  url?: string; // For redirect_url and deeplink
  content?: string; // For qr_code (Base64 string)
  metadata?: Record<string, unknown>;
  expires_at?: string; // ISO date string
  instructions?: string;
}

// Payment intent interface
export interface PaymentIntent {
  id: string;
  status: PaymentStatus;
  amount: number;
  currency: PaymentCurrency;
  description?: string;
  metadata?: Record<string, unknown>;
  created_at: string;
  updated_at: string;
  client_secret?: string;
  next_action?: PaymentNextAction;
}

// Payment method details
export interface PaymentMethodDetails {
  type: PaymentMethod;
  card?: {
    brand: string;
    last4: string;
    exp_month: number;
    exp_year: number;
    country?: string;
  };
  billing_details?: {
    name?: string;
    email?: string;
    phone?: string;
    address?: {
      line1?: string;
      line2?: string;
      city?: string;
      state?: string;
      postal_code?: string;
      country?: string;
    };
  };
}

// Enhanced payment result interface
export interface PaymentResult {
  success: boolean;
  payment_intent?: PaymentIntent;
  payment_method?: PaymentMethodDetails;
  error?: PaymentError;
  nextAction?: PaymentNextAction;
  metadata?: Record<string, unknown>;
}

// Payment error interface
export interface PaymentError {
  code: string;
  message: string;
  type: 'card_error' | 'validation_error' | 'api_error' | 'rate_limit_error' | 'authentication_error';
  param?: string;
  decline_code?: string;
  charge?: string;
}

// Stripe specific interfaces
export interface StripePaymentResult extends PaymentResult {
  paymentIntent?: {
    id: string;
    status: string;
    amount: number;
    currency: string;
    payment_method?: {
      id: string;
      type: string;
      card?: {
        brand: string;
        last4: string;
        exp_month: number;
        exp_year: number;
      };
    };
    [key: string]: unknown;
  };
  error?: {
    message: string;
    type: string;
    code?: string;
    decline_code?: string;
  };
}

// Order tracking payload
export interface OrderTrackingPayload {
  orderId: number;
  orderStatus: string;
  paymentNextAction?: PaymentNextAction;
  cancellationReason?: string;
  payment_intent_id?: string;
  amount?: number;
  currency?: PaymentCurrency;
}

// Payment form data interfaces
export interface BasePaymentFormData {
  amount: number;
  currency: PaymentCurrency;
  description?: string;
  metadata?: Record<string, unknown>;
}

export interface StripePaymentFormData extends BasePaymentFormData {
  payment_method_types: string[];
  customer_email?: string;
  billing_details?: PaymentMethodDetails['billing_details'];
}

export interface QRCodePaymentFormData extends BasePaymentFormData {
  provider: string;
  callback_url?: string;
}

export interface RedirectPaymentFormData extends BasePaymentFormData {
  return_url: string;
  cancel_url?: string;
  customer_email: string;
}

export interface DeepLinkPaymentFormData extends BasePaymentFormData {
  app_scheme: string;
  fallback_url?: string;
}

// Payment configuration interfaces
export interface PaymentConfig {
  stripe?: {
    publishable_key: string;
    api_version?: string;
    locale?: string;
  };
  supported_methods: PaymentMethod[];
  supported_currencies: PaymentCurrency[];
  default_currency: PaymentCurrency;
  min_amount: number;
  max_amount: number;
}

// Payment handler interfaces
export interface PaymentHandler {
  method: PaymentMethod;
  initialize(config: any): Promise<void>;
  createPaymentForm(data: BasePaymentFormData): Promise<React.ComponentType<any>>;
  processPayment(data: any): Promise<PaymentResult>;
  validatePaymentData(data: any): boolean;
  isSupported(): boolean;
}

// Payment service interfaces
export interface PaymentServiceConfig {
  apiBaseUrl: string;
  timeout: number;
  retryAttempts: number;
  enableLogging: boolean;
}

export interface PaymentCreateRequest {
  amount: number;
  currency: PaymentCurrency;
  payment_method: PaymentMethod;
  description?: string;
  customer_email?: string;
  metadata?: Record<string, unknown>;
  return_url?: string;
  cancel_url?: string;
}

export interface PaymentCreateResponse {
  payment_intent: PaymentIntent;
  client_secret?: string;
  next_action?: PaymentNextAction;
}

export interface PaymentStatusResponse {
  payment_intent: PaymentIntent;
  status: PaymentStatus;
  last_updated: string;
}
