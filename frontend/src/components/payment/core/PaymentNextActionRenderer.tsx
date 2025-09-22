import React from 'react';
import { PaymentNextAction } from '@/interfaces/payment.interface';
import { paymentService } from '@/services/paymentService';
import { AlertCircle } from 'lucide-react';

interface PaymentNextActionRendererProps {
  paymentNextAction: PaymentNextAction;
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
  additionalProps?: any;
}

export const PaymentNextActionRenderer: React.FC<PaymentNextActionRendererProps> = ({
  paymentNextAction,
  onPaymentComplete,
  onPaymentError,
  additionalProps = {}
}) => {
  // Check if the next action type is supported
  if (!paymentService.isNextActionSupported(paymentNextAction.type)) {
    return (
      <div className="flex flex-col items-center space-y-4 p-6 bg-red-50 rounded-lg border border-red-200">
        <AlertCircle className="w-12 h-12 text-red-500" />
        <div className="text-center">
          <h3 className="text-lg font-semibold text-red-800">Unsupported Payment Method</h3>
          <p className="text-sm text-red-600 mt-1">
            The payment method type "{paymentNextAction.type}" is not supported.
          </p>
        </div>
      </div>
    );
  }

  // Get the appropriate component for this next action type
  const Component = paymentService.getNextActionComponent(paymentNextAction);
  
  if (!Component) {
    return (
      <div className="flex flex-col items-center space-y-4 p-6 bg-yellow-50 rounded-lg border border-yellow-200">
        <AlertCircle className="w-12 h-12 text-yellow-500" />
        <div className="text-center">
          <h3 className="text-lg font-semibold text-yellow-800">Component Not Available</h3>
          <p className="text-sm text-yellow-600 mt-1">
            The component for "{paymentNextAction.type}" is not available.
          </p>
        </div>
      </div>
    );
  }

  // Get the props for this component
  const componentProps = paymentService.getNextActionProps(paymentNextAction, {
    onPaymentComplete,
    onPaymentError,
    ...additionalProps
  });

  return <Component {...componentProps} />;
};

export default PaymentNextActionRenderer;
