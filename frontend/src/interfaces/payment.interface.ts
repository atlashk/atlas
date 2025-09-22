export interface OrderTrackingPayload {
  orderId: number;
  orderStatus: string;
  paymentGatewayData?: {
    clientSecret?: string;
    [key: string]: unknown;
  };
  cancellationReason?: string;
}

export interface StripePaymentResult {
  success: boolean;
  paymentIntent?: {
    id: string;
    status: string;
    amount: number;
    currency: string;
    [key: string]: unknown;
  };
  error?: {
    message: string;
    type: string;
  };
}