import { PaymentNextAction } from '@/interfaces/payment.interface';
// import QRCodePayment from '@/components/payment/actions/QRCodePayment';
// import RedirectPayment from '@/components/payment/actions/RedirectPayment';
// import DeepLinkPayment from '@/components/payment/actions/DeepLinkPayment';
// import StripePaymentForm from '@/components/payment/methods/StripePaymentForm';

export type NextActionType = 'use_payment_element' | 'qr_code' | 'redirect_url' | 'deeplink' | 'none';

export interface NextActionComponentProps {
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
  [key: string]: unknown;
}

export class PaymentNextActionService {
  private readonly supportedTypes: NextActionType[] = [
    'use_payment_element',
    'qr_code', 
    'redirect_url',
    'deeplink',
    'none'
  ];

  /**
   * Get the appropriate component for handling different next action types
   */
  getNextActionComponent(paymentNextAction: PaymentNextAction): React.ComponentType<Record<string, unknown>> | null {
    switch (paymentNextAction.type) {
      case 'use_payment_element':
        // Return the payment form component for the specific provider
        if (paymentNextAction.provider === 'stripe') {
          // return StripePaymentForm;
          return null; // Temporarily disabled
        }
        return null;

      case 'qr_code':
        // return QRCodePayment;
        return null; // Temporarily disabled

      case 'redirect_url':
        // return RedirectPayment;
        return null; // Temporarily disabled

      case 'deeplink':
        // return DeepLinkPayment;
        return null; // Temporarily disabled

      default:
        console.warn(`Unsupported next action type: ${paymentNextAction.type}`);
        return null;
    }
  }

  /**
   * Get props for the next action component
   */
  getNextActionProps(
    paymentNextAction: PaymentNextAction, 
    additionalProps: NextActionComponentProps = {}
  ): Record<string, unknown> {
    const baseProps = {
      onPaymentComplete: additionalProps.onPaymentComplete,
      onPaymentError: additionalProps.onPaymentError,
      ...additionalProps
    };

    switch (paymentNextAction.type) {
      case 'use_payment_element':
        return {
          ...baseProps,
          clientSecret: paymentNextAction.client_secret,
          publishableKey: paymentNextAction.publishable_key,
        };

      case 'qr_code':
        return {
          ...baseProps,
          content: paymentNextAction.content || '',
        };

      case 'redirect_url':
        return {
          ...baseProps,
          url: paymentNextAction.url || '',
          autoRedirect: additionalProps.autoRedirect || false,
        };

      case 'deeplink':
        return {
          ...baseProps,
          url: paymentNextAction.url || '',
          autoLaunch: additionalProps.autoLaunch || false,
        };

      default:
        return baseProps;
    }
  }

  /**
   * Check if a next action type is supported
   */
  isNextActionSupported(type: string): boolean {
    return this.supportedTypes.includes(type as NextActionType);
  }

  /**
   * Validate next action data
   */
  validateNextAction(paymentNextAction: PaymentNextAction): { isValid: boolean; errors: string[] } {
    const errors: string[] = [];

    if (!paymentNextAction.type) {
      errors.push('Next action type is required');
    } else if (!this.isNextActionSupported(paymentNextAction.type)) {
      errors.push(`Unsupported next action type: ${paymentNextAction.type}`);
    }

    // Type-specific validations
    switch (paymentNextAction.type) {
      case 'use_payment_element':
        if (!paymentNextAction.client_secret) {
          errors.push('Client secret is required for payment element');
        }
        if (!paymentNextAction.publishable_key) {
          errors.push('Publishable key is required for payment element');
        }
        if (!paymentNextAction.provider) {
          errors.push('Provider is required for payment element');
        }
        break;

      case 'qr_code':
        if (!paymentNextAction.content) {
          errors.push('Content is required for QR code');
        }
        break;

      case 'redirect_url':
      case 'deeplink':
        if (!paymentNextAction.url) {
          errors.push(`URL is required for ${paymentNextAction.type}`);
        }
        break;
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  /**
   * Get supported next action types
   */
  getSupportedTypes(): NextActionType[] {
    return [...this.supportedTypes];
  }
}

export const paymentNextActionService = new PaymentNextActionService();
