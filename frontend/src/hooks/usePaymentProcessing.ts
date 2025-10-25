import { PaymentNextAction, PaymentNextActionResponse } from "@/interfaces/payment.interface";
import { paymentApi } from "@/api/payment.api";
import { useCallback, useMemo, useState } from "react";
import { useErrorHandler } from "./useErrorHandler";

export function usePaymentProcessing() {
  const [isLoading, setIsLoading] = useState(false);
  const { error, handleApiError, clearError } = useErrorHandler();

  const fetchPaymentNextAction = useCallback(
    async (orderId: string): Promise<PaymentNextActionResponse | null> => {
      if (!orderId) {
        handleApiError(new Error("Order ID is required"));
        return null;
      }

      setIsLoading(true);
      clearError();

      try {
        const response = await paymentApi.getPaymentNextAction(orderId);
        return response.data;
      } catch (err) {
        handleApiError(err as Error);
        return null;
      } finally {
        setIsLoading(false);
      }
    },
    [handleApiError, clearError]
  );

  const memoizedReturn = useMemo(
    () => ({
      isLoading,
      error,
      fetchPaymentNextAction,
      clearError,
    }),
    [isLoading, error, fetchPaymentNextAction, clearError]
  );

  return memoizedReturn;
}
