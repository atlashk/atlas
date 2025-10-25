import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { paymentApi } from "@/api/index.api";
import { PaymentMethod } from "@/constants";
import { useErrorHandler } from "./useErrorHandler";

export function usePaymentMethods(defaultMethod: PaymentMethod = "CARD") {
  const [availablePaymentMethods, setAvailablePaymentMethods] = useState<string[]>([]);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod>(defaultMethod);
  const [isLoading, setIsLoading] = useState(true);
  const { error, handleApiError, clearError } = useErrorHandler();
  
  // Prevent duplicate API calls during development mode double renders
  const fetchedRef = useRef(false);

  const fetchPaymentMethods = useCallback(async () => {
    if (fetchedRef.current) return;

    try {
      setIsLoading(true);
      clearError();
      fetchedRef.current = true;
      
      const response = await paymentApi.getPaymentMethods();
      
      if (response.success && response.data) {
        setAvailablePaymentMethods(response.data);
        
        // Set first available method as default if current selection is not available
        if (response.data.length > 0 && !response.data.includes(selectedPaymentMethod)) {
          setSelectedPaymentMethod(response.data[0] as PaymentMethod);
        }
      } else {
        throw new Error("Failed to fetch payment methods");
      }
    } catch (err) {
      handleApiError(err as Error);
      
      // Fallback to default methods if API fails
      setAvailablePaymentMethods(["CARD", "PAYPAL"]);
      fetchedRef.current = false; // Allow retry on error
    } finally {
      setIsLoading(false);
    }
  }, [selectedPaymentMethod, handleApiError, clearError]);

  useEffect(() => {
    fetchPaymentMethods();
  }, [fetchPaymentMethods]);

  const retry = useCallback(() => {
    fetchedRef.current = false;
    clearError();
    // Trigger re-fetch by updating a dependency
    setIsLoading(true);
  }, [clearError]);

  const handlePaymentMethodSelect = useCallback((method: PaymentMethod) => {
    setSelectedPaymentMethod(method);
    clearError(); // Clear any previous errors when user selects a method
  }, [clearError]);

  const memoizedReturn = useMemo(() => ({
    availablePaymentMethods,
    selectedPaymentMethod,
    setSelectedPaymentMethod: handlePaymentMethodSelect,
    isLoading,
    error,
    retry,
  }), [availablePaymentMethods, selectedPaymentMethod, handlePaymentMethodSelect, isLoading, error, retry]);

  return memoizedReturn;
}
