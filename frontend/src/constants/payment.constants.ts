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

export enum UsePaymentElementProvider {
  STRIPE = "STRIPE",
}

// Frontend payment providers (for UI handling)
export const PAYMENT_PROVIDERS = [
  "stripe",
  "qr_code",
  "redirect",
  "deep_link",
] as const;

export type PaymentProvider = (typeof PAYMENT_PROVIDERS)[number];
