export interface PaymentNextAction {
  type: string;
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
  code: string;
  name: string;
}

// Payment next action response interface
export interface PaymentNextActionResponse {
  nextAction?: PaymentNextAction;
  amount?: number;
  currency?: string;
}
