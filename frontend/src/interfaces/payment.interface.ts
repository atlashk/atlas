// Re-export types from constants to avoid duplication
export type { PaymentNextActionType, PaymentStatus, UsePaymentElementProvider } from '@/constants';
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

// Payment gateway response interface
export interface PaymentGatewayResponse {
  id: number;
  name: string;
}

// Payment next action response interface
export interface PaymentNextActionResponse {
  nextAction?: PaymentNextAction;
  amount?: number;
  currency?: string;
}
