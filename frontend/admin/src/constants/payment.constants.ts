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
