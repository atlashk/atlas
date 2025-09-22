export interface OrderTrackingPayload {
  orderId: number;
  orderStatus: string;
  paymentGatewayData?: {
    clientSecret?: string;
    [key: string]: any;
  };
  cancellationReason?: string;
}

export interface StripePaymentResult {
  success: boolean;
  paymentIntent?: any;
  error?: {
    message: string;
    type: string;
  };
}