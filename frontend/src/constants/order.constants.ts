export const ORDER_STATUSES = [
  "AWAITING_PRODUCT_RESERVATION",
  "AWAITING_PAYMENT",
  "FULFILLED",
  "CANCELED"
] as const;

export type OrderStatus = typeof ORDER_STATUSES[number];
