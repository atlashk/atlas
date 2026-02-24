export const ORDER_STATUSES = [
  "AWAITING_STOCK_RESERVATION",
  "AWAITING_PAYMENT_INITIALIZED",
  "AWAITING_PAYMENT_PROCESSED",
  "FULFILLED",
  "CANCELED"
] as const;

export type OrderStatus = typeof ORDER_STATUSES[number];

// Checkout constants
export const ORDER_STATUS_MESSAGES = {
  AWAITING_STOCK_RESERVATION: "Reserving stock...",
  AWAITING_PAYMENT_INITIALIZED: "Initializing payment...",
  AWAITING_PAYMENT_PROCESSED: "Processing payment...",
  FULFILLED: "Order Completed!",
  CANCELED: "Order Canceled",
} as const;
