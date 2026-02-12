export const ORDER_STATUSES = [
  "AWAITING_PRODUCT_RESERVATION",
  "AWAITING_PAYMENT_INITIALIZED",
  "AWAITING_PAYMENT_PROCESSED",
  "FULFILLED",
  "CANCELED"
] as const;

export type OrderStatus = typeof ORDER_STATUSES[number];

// Checkout constants

export const ORDER_STATUS_MESSAGES = {
  AWAITING_PRODUCT_RESERVATION: "Reserving products...",
  AWAITING_PAYMENT_INITIALIZED: "Initializing payment...",
  AWAITING_PAYMENT_PROCESSED: "Processing payment...",
  FULFILLED: "Order Completed!",
  CANCELED: "Order Canceled",
} as const;

export const ORDER_STATUS_DESCRIPTIONS = {
  AWAITING_PRODUCT_RESERVATION: "Creating your order...",
  AWAITING_PAYMENT_INITIALIZED: "Updating order status...",
  AWAITING_PAYMENT_PROCESSED: "Updating order status...",
  FULFILLED: "Your order has been processed successfully.",
  CANCELED: "Your order has been canceled.",
} as const;
