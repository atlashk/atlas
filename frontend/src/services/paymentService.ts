import { configStore } from '@/lib/config';
import { loadStripe, Stripe, StripeElements, ConfirmPaymentData } from '@stripe/stripe-js';
import StripePaymentForm from '@/components/front/StripePaymentForm';

export interface PaymentGatewayHandler {
  initialize(): Promise<void>;
  createPaymentForm(): React.ComponentType<PaymentFormProps>;
  processPayment(paymentData: PaymentData): Promise<PaymentResult>;
}

export interface PaymentFormProps {
  clientSecret: string;
  orderId: string;
  onPaymentResult: (result: PaymentResult) => void;
  onCancel: () => void;
}

export interface PaymentData {
  elements: StripeElements;
  clientSecret: string;
  confirmParams: ConfirmPaymentData;
}

export interface PaymentResult {
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

class StripePaymentHandler implements PaymentGatewayHandler {
  private stripe: Stripe | null = null;

  async initialize(): Promise<void> {
    const config = configStore.getPaymentConfig();
    const stripeConfig = config.gateways.stripe;
    
    if (!stripeConfig?.enabled || !stripeConfig.publishableKey) {
      throw new Error('Stripe is not configured or enabled');
    }

    this.stripe = await loadStripe(stripeConfig.publishableKey);
    if (!this.stripe) {
      throw new Error('Failed to initialize Stripe');
    }
  }

  createPaymentForm(): React.ComponentType<PaymentFormProps> {
    // Return the existing StripePaymentForm component
    return StripePaymentForm;
  }

  async processPayment(paymentData: PaymentData): Promise<PaymentResult> {
    if (!this.stripe) {
      throw new Error('Stripe not initialized');
    }

    try {
      const { error } = await this.stripe.confirmPayment(paymentData);
      
      if (error) {
        return {
          success: false,
          error: {
            message: error.message || 'Payment failed',
            type: error.type || 'unknown',
          },
        };
      }

      // If no error, payment was successful
      return {
        success: true,
      };
    } catch {
      return {
        success: false,
        error: {
          message: 'An unexpected error occurred',
          type: 'unknown',
        },
      };
    }
  }
}



class PaymentService {
  private handlers: Map<string, PaymentGatewayHandler> = new Map();
  private initialized = false;

  constructor() {
    this.handlers.set('stripe', new StripePaymentHandler());
  }

  async initialize(): Promise<void> {
    if (this.initialized) return;

    const config = configStore.getPaymentConfig();
    const defaultGateway = config.defaultGateway;
    
    const handler = this.handlers.get(defaultGateway);
    if (!handler) {
      throw new Error(`Payment gateway '${defaultGateway}' not supported`);
    }

    await handler.initialize();
    this.initialized = true;
  }

  getPaymentHandler(gateway?: string): PaymentGatewayHandler {
    const config = configStore.getPaymentConfig();
    const gatewayToUse = gateway || config.defaultGateway;
    
    const handler = this.handlers.get(gatewayToUse);
    if (!handler) {
      throw new Error(`Payment gateway '${gatewayToUse}' not supported`);
    }

    return handler;
  }

  async createPaymentForm(
    gateway?: string
  ): Promise<React.ComponentType<PaymentFormProps>> {
    const handler = this.getPaymentHandler(gateway);
    return handler.createPaymentForm();
  }

  async processPayment(paymentData: PaymentData, gateway?: string): Promise<PaymentResult> {
    const handler = this.getPaymentHandler(gateway);
    return handler.processPayment(paymentData);
  }

  changeDefaultGateway(gateway: 'stripe'): void {
    configStore.setDefaultPaymentGateway(gateway);
    this.initialized = false; // Force re-initialization
  }

  getSupportedGateways(): string[] {
    const config = configStore.getPaymentConfig();
    return Object.entries(config.gateways)
      .filter(([, gatewayConfig]) => gatewayConfig?.enabled)
      .map(([gateway]) => gateway);
  }
}

export const paymentService = new PaymentService();