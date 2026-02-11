export const PAYMENT_METHOD_LABELS: Record<string, string> = {
  CARD: "Credit/Debit Card",
  PAYPAL: "PayPal",
  BANK_TRANSFER: "Bank Transfer",
  E_WALLET: "E-Wallet",
};

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

export const PAYMENT_INSTRUCTIONS = {
  QR_CODE: [
    "Open your mobile banking app",
    'Select "Scan QR Code" or "Pay by QR"',
    "Point your camera at the QR code above",
    "Confirm the payment amount",
    "Complete the payment",
  ],
} as const;

export const getPaymentMethodLabel = (method: string): string => {
  return PAYMENT_METHOD_LABELS[method] || method;
};
