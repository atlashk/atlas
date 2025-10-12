import { PaymentNextAction } from '@/interfaces/payment.interface';
import { 
  paymentGatewayService,
  paymentNextActionService,
  paymentComponentService,
  PaymentGatewayHandler,
  PaymentFormProps,
  PaymentData,
  PaymentResult,
  NextActionComponentProps
} from './payment';

/**
 * Legacy PaymentService - delegates to new modular services
 * @deprecated Use individual payment services instead
 */
class PaymentService {
  // Delegate to PaymentGatewayService
  async initializeHandler(paymentNextAction: PaymentNextAction): Promise<PaymentGatewayHandler> {
    return paymentGatewayService.initializeHandler(paymentNextAction);
  }

  async createPaymentForm(
    paymentNextAction: PaymentNextAction
  ): Promise<React.ComponentType<PaymentFormProps> | null> {
    return paymentGatewayService.createPaymentForm(paymentNextAction);
  }

  async processPayment(
    paymentData: PaymentData, 
    paymentNextAction: PaymentNextAction
  ): Promise<PaymentResult> {
    return paymentGatewayService.processPayment(paymentData, paymentNextAction);
  }

  getSupportedProviders(): string[] {
    return paymentGatewayService.getSupportedProviders();
  }

  clearInitializedHandlers(): void {
    paymentGatewayService.clearInitializedHandlers();
  }

  // Delegate to PaymentNextActionService
  getNextActionComponent(paymentNextAction: PaymentNextAction): React.ComponentType<Record<string, unknown>> | null {
    return paymentNextActionService.getNextActionComponent(paymentNextAction);
  }

  getNextActionProps(paymentNextAction: PaymentNextAction, additionalProps: NextActionComponentProps = {}): Record<string, unknown> {
    return paymentNextActionService.getNextActionProps(paymentNextAction, additionalProps);
  }

  isNextActionSupported(type: string): boolean {
    return paymentNextActionService.isNextActionSupported(type);
  }

  // Delegate to PaymentComponentService
  getComponentConfig(paymentNextAction: PaymentNextAction, additionalProps: NextActionComponentProps = {}) {
    return paymentComponentService.getComponentConfig(paymentNextAction, additionalProps);
  }

  renderComponent(paymentNextAction: PaymentNextAction, additionalProps: NextActionComponentProps = {}) {
    return paymentComponentService.renderComponent(paymentNextAction, additionalProps);
  }

  canRender(paymentNextAction: PaymentNextAction): boolean {
    return paymentComponentService.canRender(paymentNextAction);
  }
}

export const paymentService = new PaymentService();

// Re-export types for backward compatibility
export type { PaymentFormProps, PaymentResult } from './payment';
