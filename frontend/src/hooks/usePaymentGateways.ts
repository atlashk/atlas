import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { paymentApi } from "@/api/index.api";
import { PaymentGatewayResponse } from "@/interfaces/payment.interface";
import { useErrorHandler } from "./useErrorHandler";

export function usePaymentGateways() {
  const [availablePaymentGateways, setAvailablePaymentGateways] = useState<PaymentGatewayResponse[]>([]);
  const [selectedPaymentGateway, setSelectedPaymentGateway] = useState<PaymentGatewayResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const { error, handleApiError, clearError } = useErrorHandler();
  
  // Prevent duplicate API calls during development mode double renders
  const fetchedRef = useRef(false);

  const fetchPaymentGateways = useCallback(async () => {
    if (fetchedRef.current) return;

    try {
      setIsLoading(true);
      clearError();
      fetchedRef.current = true;
      
      const response = await paymentApi.getPaymentGateways();
      
      if (response.success && response.data) {
        setAvailablePaymentGateways(response.data);
        
        // Set first available gateway as default if no selection or current selection is not available
        if (response.data.length > 0 && (!selectedPaymentGateway || !response.data.find(g => g.id === selectedPaymentGateway.id))) {
          setSelectedPaymentGateway(response.data[0]);
        }
      } else {
        throw new Error("Failed to fetch payment gateways");
      }
    } catch (err) {
      handleApiError(err as Error);
      
      // Reset state on error
      setAvailablePaymentGateways([]);
      setSelectedPaymentGateway(null);
      fetchedRef.current = false; // Allow retry on error
    } finally {
      setIsLoading(false);
    }
  }, [selectedPaymentGateway, handleApiError, clearError]);

  useEffect(() => {
    fetchPaymentGateways();
  }, [fetchPaymentGateways]);

  const retry = useCallback(() => {
    fetchedRef.current = false;
    clearError();
    // Trigger re-fetch by updating a dependency
    setIsLoading(true);
  }, [clearError]);

  const handlePaymentGatewaySelect = useCallback((gateway: PaymentGatewayResponse) => {
    setSelectedPaymentGateway(gateway);
    clearError(); // Clear any previous errors when user selects a gateway
  }, [clearError]);

  const memoizedReturn = useMemo(() => ({
    availablePaymentGateways,
    selectedPaymentGateway,
    setSelectedPaymentGateway: handlePaymentGatewaySelect,
    isLoading,
    error,
    retry,
  }), [availablePaymentGateways, selectedPaymentGateway, handlePaymentGatewaySelect, isLoading, error, retry]);

  return memoizedReturn;
}
