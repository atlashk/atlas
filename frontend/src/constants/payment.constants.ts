// Payment constants (mapped to backend)
export const PAYMENT_STATUSES = [
  "PENDING",
  "CREATED",
  "SUCCEEDED",
  "CANCELED",
  "FAILED",
  "UNKNOWN",
] as const;

export type PaymentStatus = (typeof PAYMENT_STATUSES)[number];

export const PAYMENT_NEXT_ACTION_TYPES = [
  "REDIRECT_URL",
  "DEEPLINK",
  "QR_CODE",
  "USE_PAYMENT_ELEMENT",
] as const;

export type PaymentNextActionType = (typeof PAYMENT_NEXT_ACTION_TYPES)[number];

// Payment gateway codes (mapped to backend)
export const PAYMENT_GATEWAY_CODES = [
  "STRIPE",
  "SIMULATOR",
] as const;

export type PaymentGatewayCode = (typeof PAYMENT_GATEWAY_CODES)[number];

// Supported payment gateways for UI rendering
export const SUPPORTED_PAYMENT_GATEWAYS: PaymentGatewayCode[] = ["STRIPE"];
