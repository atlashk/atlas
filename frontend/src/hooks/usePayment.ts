import { useState, useCallback, useRef } from 'react';
import { paymentGatewayService, paymentNextActionService, paymentComponentService } from '@/services/payment';
import type { PaymentMethod, PaymentResult, PaymentNextAction } from '@/services/payment';

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
    processPayment: (paymentData: any) => Promise<void>;
    handleNextAction: (action: PaymentNextAction) => Promise<void>;
    resetPayment: () => void;
    setPaymentMethod: (method: PaymentMethod) => void;
  };
  components: {
    getPaymentForm: () => React.ComponentType<any> | null;
    getNextActionComponent: () => React.ComponentType<any> | null;
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
    method: PaymentMethod,
    amount: number,
    currency: string
  ) => {
    try {
      updateState({ isLoading: true, error: null });
      
      abortControllerRef.current = new AbortController();
      
      await paymentGatewayService.initializeHandler(method);
      updateState({ paymentMethod: method, isLoading: false });
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to initialize payment';
      updateState({ error: errorMessage, isLoading: false });
      onError?.(errorMessage);
    }
  }, [updateState, onError]);

  const processPayment = useCallback(async (paymentData: any) => {
    if (!state.paymentMethod) {
      const error = 'No payment method selected';
      updateState({ error });
      onError?.(error);
      return;
    }

    try {
      updateState({ isProcessing: true, error: null });
      
      const result = await paymentGatewayService.processPayment(state.paymentMethod, paymentData);
      
      if (result.success) {
        updateState({ 
          paymentResult: result, 
          isProcessing: false,
          nextAction: result.nextAction || null
        });
        onSuccess?.(result);
        
        if (result.nextAction) {
          onNextAction?.(result.nextAction);
        }
      } else {
        const errorMessage = result.error || 'Payment processing failed';
        updateState({ error: errorMessage, isProcessing: false });
        onError?.(errorMessage);
      }
      
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Payment processing failed';
      updateState({ error: errorMessage, isProcessing: false });
      onError?.(errorMessage);
    }
  }, [state.paymentMethod, updateState, onSuccess, onError, onNextAction]);

  const handleNextAction = useCallback(async (action: PaymentNextAction) => {
    try {
      updateState({ isLoading: true, error: null });
      
      const isSupported = paymentNextActionService.isActionSupported(action.type);
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

  const getPaymentForm = useCallback(() => {
    if (!state.paymentMethod) return null;
    
    const config = paymentComponentService.getComponentConfig(state.paymentMethod);
    return config?.component || null;
  }, [state.paymentMethod]);

  const getNextActionComponent = useCallback(() => {
    if (!state.nextAction) return null;
    
    return paymentNextActionService.getComponent(state.nextAction.type);
  }, [state.nextAction]);

  const canRenderPaymentForm = useCallback(() => {
    if (!state.paymentMethod) return false;
    return paymentComponentService.canRenderComponent(state.paymentMethod);
  }, [state.paymentMethod]);

  const canRenderNextAction = useCallback(() => {
    if (!state.nextAction) return false;
    return paymentNextActionService.isActionSupported(state.nextAction.type);
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