import { loadStripe, Stripe, StripeElements, ConfirmPaymentData } from '@stripe/stripe-js';
import StripePaymentForm from '@/components/payment/methods/StripePaymentForm';
import { PaymentNextAction } from '@/interfaces/payment.interface';

export interface PaymentGatewayHandler {
  initialize(paymentNextAction: PaymentNextAction): Promise<void>;
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
  private publishableKey: string | null = null;

  async initialize(paymentNextAction: PaymentNextAction): Promise<void> {
    if (!paymentNextAction.publishable_key) {
      throw new Error('Stripe publishable key not provided');
    }

    this.publishableKey = paymentNextAction.publishable_key;
    this.stripe = await loadStripe(this.publishableKey);
    
    if (!this.stripe) {
      throw new Error('Failed to load Stripe');
    }
  }

  createPaymentForm(): React.ComponentType<PaymentFormProps> {
    return StripePaymentForm;
  }

  async processPayment(paymentData: PaymentData): Promise<PaymentResult> {
    if (!this.stripe) {
      throw new Error('Stripe not initialized');
    }

    try {
      const { error, paymentIntent } = await this.stripe.confirmPayment({
        elements: paymentData.elements,
        clientSecret: paymentData.clientSecret,
        confirmParams: paymentData.confirmParams,
        redirect: 'if_required',
      });

      if (error) {
        return {
          success: false,
          error: {
            message: error.message || 'Payment failed',
            type: error.type || 'unknown_error',
          },
        };
      }

      return {
        success: true,
        paymentIntent: paymentIntent ? {
          id: paymentIntent.id,
          status: paymentIntent.status,
          amount: paymentIntent.amount,
          currency: paymentIntent.currency,
        } : undefined,
      };
    } catch (error) {
      return {
        success: false,
        error: {
          message: error instanceof Error ? error.message : 'Unknown error occurred',
          type: 'unknown_error',
        },
      };
    }
  }
}

export class PaymentGatewayService {
  private handlers: Map<string, PaymentGatewayHandler> = new Map();
  private initializedHandlers: Map<string, PaymentGatewayHandler> = new Map();

  constructor() {
    this.handlers.set('stripe', new StripePaymentHandler());
  }

  async initializeHandler(paymentNextAction: PaymentNextAction): Promise<PaymentGatewayHandler> {
    if (!paymentNextAction.provider) {
      throw new Error('Payment provider not specified in paymentNextAction');
    }

    const provider = paymentNextAction.provider.toLowerCase();
    const handlerKey = `${provider}-${paymentNextAction.client_secret}`;
    
    // Return already initialized handler if exists
    if (this.initializedHandlers.has(handlerKey)) {
      return this.initializedHandlers.get(handlerKey)!;
    }

    const handler = this.handlers.get(provider);
    if (!handler) {
      throw new Error(`Payment gateway '${provider}' not supported`);
    }

    await handler.initialize(paymentNextAction);
    this.initializedHandlers.set(handlerKey, handler);
    
    return handler;
  }

  async createPaymentForm(
    paymentNextAction: PaymentNextAction
  ): Promise<React.ComponentType<PaymentFormProps>> {
    const handler = await this.initializeHandler(paymentNextAction);
    return handler.createPaymentForm();
  }

  async processPayment(
    paymentData: PaymentData, 
    paymentNextAction: PaymentNextAction
  ): Promise<PaymentResult> {
    const handler = await this.initializeHandler(paymentNextAction);
    return handler.processPayment(paymentData);
  }

  getSupportedProviders(): string[] {
    return Array.from(this.handlers.keys());
  }

  clearInitializedHandlers(): void {
    this.initializedHandlers.clear();
  }
}

export const paymentGatewayService = new PaymentGatewayService();