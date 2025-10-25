// Re-export types from constants to avoid duplication
export type { PaymentMethod, PaymentNextActionType, PaymentStatus, UsePaymentElementProvider } from '@/constants';
import type { PaymentNextActionType, UsePaymentElementProvider } from '@/constants';

export interface PaymentNextAction {
  type: PaymentNextActionType;
  provider?: UsePaymentElementProvider; // For use_payment_element
  clientSecret?: string;
  publishableKey?: string;
  url?: string; // For redirect_url and deeplink
  content?: string; // For qr_code (Base64 string)
  amount?: number; // Payment amount
  currency?: string; // Payment currency
}

// Backend returns List<String> of payment method names
export type PaymentMethodResponse = string[];

// Payment next action response interface
export interface PaymentNextActionResponse {
  nextAction?: PaymentNextAction;
  amount?: number;
  currency?: string;
}
