import { PaymentNextAction } from "@/interfaces/payment.interface";
import { useCallback, useMemo, useState } from "react";

interface CheckoutState {
  orderId: string | null;
  isCheckingOut: boolean;
  paymentNextAction: PaymentNextAction | null;
  isProcessingPayment: boolean;
}

const initialState: CheckoutState = {
  orderId: null,
  isCheckingOut: false,
  paymentNextAction: null,
  isProcessingPayment: false,
};

export function useCheckoutState() {
  const [state, setState] = useState<CheckoutState>(initialState);

  const updateState = useCallback((updates: Partial<CheckoutState>) => {
    setState((prev) => ({ ...prev, ...updates }));
  }, []);

  const resetState = useCallback(() => {
    setState(initialState);
  }, []);

  const setOrderId = useCallback((orderId: string | null) => {
    updateState({ orderId });
  }, [updateState]);

  const setIsCheckingOut = useCallback((isCheckingOut: boolean) => {
    updateState({ isCheckingOut });
  }, [updateState]);

  const setPaymentNextAction = useCallback(
    (paymentNextAction: PaymentNextAction | null) => {
      updateState({ paymentNextAction });
    },
    [updateState]
  );

  const setIsProcessingPayment = useCallback((isProcessingPayment: boolean) => {
    updateState({ isProcessingPayment });
  }, [updateState]);

  const memoizedReturn = useMemo(
    () => ({
      ...state,
      updateState,
      resetState,
      setOrderId,
      setIsCheckingOut,
      setPaymentNextAction,
      setIsProcessingPayment,
    }),
    [
      state,
      updateState,
      resetState,
      setOrderId,
      setIsCheckingOut,
      setPaymentNextAction,
      setIsProcessingPayment,
    ]
  );

  return memoizedReturn;
}
