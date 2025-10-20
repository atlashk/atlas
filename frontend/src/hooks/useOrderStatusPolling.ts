import { useState, useEffect, useRef, useCallback } from 'react';
import { orderApi } from '@/api/order.api';
import { OrderStatusResponse } from '@/interfaces/order.interface';

export function useOrderStatusPolling(orderId: string | null) {
  const [orderStatus, setOrderStatus] = useState<OrderStatusResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  const startPolling = useCallback(() => {
    if (!orderId) return;

    setIsLoading(true);
    setError(null);

    const poll = async () => {
      try {
        const response = await orderApi.getOrderStatus(orderId);
        
        if (response.success && response.data) {
          setOrderStatus(response.data);

          // Stop polling if order is completed (fulfilled or canceled)
          // Continue polling for AWAITING_PAYMENT_PROCESSED to allow payment processing
          if (['FULFILLED', 'CANCELED'].includes(response.data.status)) {
            stopPolling();
            setIsLoading(false);
          }
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
        stopPolling();
        setIsLoading(false);
      }
    };

    // Poll immediately
    poll();

    // Then poll every 2 seconds
    intervalRef.current = setInterval(poll, 2000);
  }, [orderId]);

  const stopPolling = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  };

  const reset = () => {
    stopPolling();
    setOrderStatus(null);
    setIsLoading(false);
    setError(null);
  };

  useEffect(() => {
    if (orderId) {
      startPolling();
    }

    return () => {
      stopPolling();
    };
  }, [orderId, startPolling]);

  // Helper function to check if payment processing is needed
  const needsPaymentProcessing = orderStatus?.status === 'AWAITING_PAYMENT_PROCESSED';

  return {
    orderStatus,
    isLoading,
    error,
    needsPaymentProcessing,
    startPolling,
    stopPolling,
    reset
  };
}