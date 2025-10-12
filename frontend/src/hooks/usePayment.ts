import { useState, useCallback, useRef } from 'react';
import { paymentGatewayService, paymentNextActionService } from '@/services/payment';
import type { PaymentResult, PaymentData, PaymentFormProps } from '@/services/payment';
import type { PaymentMethod, PaymentNextAction } from '@/interfaces/payment.interface';

export interface PaymentState {
  isLoading: boolean;
  error: string | null;
  paymentMethod: PaymentMethod | null;
  paymentResult: PaymentResult | null;
  nextAction: PaymentNextAction | null;
  isProcessing: boolean;
}

export interface UsePaymentOptions {
  onSuccess?: (result: PaymentResult) => void;
  onError?: (error: string) => void;
  onNextAction?: (action: PaymentNextAction) => void;
}

export interface UsePaymentReturn {
  state: PaymentState;
  actions: {
    initializePayment: (method: PaymentMethod, amount: number, currency: string) => Promise<void>;
    processPayment: (paymentData: PaymentData) => Promise<void>;
    handleNextAction: (action: PaymentNextAction) => Promise<void>;
    resetPayment: () => void;
    setPaymentMethod: (method: PaymentMethod) => void;
  };
  components: {
    getPaymentForm: () => Promise<React.ComponentType<PaymentFormProps> | null>;
    getNextActionComponent: () => React.ComponentType<Record<string, unknown>> | null;
    canRenderPaymentForm: () => boolean;
    canRenderNextAction: () => boolean;
  };
}

export function usePayment(options: UsePaymentOptions = {}): UsePaymentReturn {
  const { onSuccess, onError, onNextAction } = options;
  
  const [state, setState] = useState<PaymentState>({
    isLoading: false,
    error: null,
    paymentMethod: null,
    paymentResult: null,
    nextAction: null,
    isProcessing: false,
  });

  const abortControllerRef = useRef<AbortController | null>(null);

  const updateState = useCallback((updates: Partial<PaymentState>) => {
    setState(prev => ({ ...prev, ...updates }));
  }, []);

  const resetPayment = useCallback(() => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      abortControllerRef.current = null;
    }
    
    setState({
      isLoading: false,
      error: null,
      paymentMethod: null,
      paymentResult: null,
      nextAction: null,
      isProcessing: false,
    });
  }, []);

  const setPaymentMethod = useCallback((method: PaymentMethod) => {
    updateState({ paymentMethod: method, error: null });
  }, [updateState]);

  const initializePayment = useCallback(async (
    method: PaymentMethod
  ) => {
    try {
      updateState({ isLoading: true, error: null });
      
      abortControllerRef.current = new AbortController();
      
      // Just set the payment method - handler initialization happens when processing payment
      updateState({ paymentMethod: method, isLoading: false });
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to initialize payment';
      updateState({ error: errorMessage, isLoading: false });
      onError?.(errorMessage);
    }
  }, [updateState, onError]);

  const processPayment = useCallback(async (paymentData: PaymentData) => {
    if (!state.nextAction) {
      const error = 'No payment next action available';
      updateState({ error });
      onError?.(error);
      return;
    }

    try {
      updateState({ isProcessing: true, error: null });
      
      const result = await paymentGatewayService.processPayment(paymentData, state.nextAction);
      
      if (result.success) {
        updateState({ 
          paymentResult: result, 
          isProcessing: false
        });
        onSuccess?.(result);
      } else {
        const errorMessage = result.error?.message || 'Payment processing failed';
        updateState({ error: errorMessage, isProcessing: false });
        onError?.(errorMessage);
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Payment processing failed';
      updateState({ error: errorMessage, isProcessing: false });
      onError?.(errorMessage);
    }
  }, [state.nextAction, updateState, onSuccess, onError]);

  const handleNextAction = useCallback(async (action: PaymentNextAction) => {
    try {
      updateState({ isLoading: true, error: null });
      
      const isSupported = paymentNextActionService.isNextActionSupported(action.type);
      if (!isSupported) {
        throw new Error(`Unsupported next action type: ${action.type}`);
      }

      // Handle the next action based on its type
      // This would typically involve rendering the appropriate component
      // and waiting for user interaction or automatic processing
      
      updateState({ nextAction: action, isLoading: false });
      onNextAction?.(action);
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to handle next action';
      updateState({ error: errorMessage, isLoading: false });
      onError?.(errorMessage);
    }
  }, [updateState, onError, onNextAction]);

  const getPaymentForm = useCallback(async () => {
    if (!state.nextAction) return null;
    
    try {
      return await paymentGatewayService.createPaymentForm(state.nextAction);
    } catch (error) {
      console.error('Failed to create payment form:', error);
      return null;
    }
  }, [state.nextAction]);

  const getNextActionComponent = useCallback(() => {
    if (!state.nextAction) return null;
    
    return paymentNextActionService.getNextActionComponent(state.nextAction);
  }, [state.nextAction]);

  const canRenderPaymentForm = useCallback(() => {
    if (!state.nextAction) return false;
    return paymentNextActionService.isNextActionSupported(state.nextAction.type);
  }, [state.nextAction]);

  const canRenderNextAction = useCallback(() => {
    if (!state.nextAction) return false;
    return paymentNextActionService.isNextActionSupported(state.nextAction.type);
  }, [state.nextAction]);

  return {
    state,
    actions: {
      initializePayment,
      processPayment,
      handleNextAction,
      resetPayment,
      setPaymentMethod,
    },
    components: {
      getPaymentForm,
      getNextActionComponent,
      canRenderPaymentForm,
      canRenderNextAction,
    },
  };
}