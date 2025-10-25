import { useState, useEffect, useRef, useCallback } from 'react';
import { orderApi } from '@/api/order.api';
import { OrderStatusResponse } from '@/interfaces/order.interface';

export function useOrderStatusPolling(orderId: string | null) {
  const [orderStatus, setOrderStatus] = useState<OrderStatusResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const isPollingRef = useRef<boolean>(false);

  const stopPolling = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    isPollingRef.current = false;
  }, [orderId]);

  const startPolling = useCallback(() => {
    if (!orderId) return;
    
    // Prevent multiple polling instances
    if (isPollingRef.current) {
      return;
    }

    // Stop any existing polling first
    stopPolling();

    isPollingRef.current = true;
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
            // Use functional update to ensure we have the latest state
            setIsLoading(() => false);
            
            stopPolling();
            return; // Exit early to prevent further polling
          }
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
        setIsLoading(false);
        stopPolling();
      }
    };

    // Poll immediately
    poll();

    // Then poll every 2 seconds
    intervalRef.current = setInterval(poll, 2000);
  }, [orderId, stopPolling]);

  const reset = useCallback(() => {
    stopPolling();
    setOrderStatus(null);
    setError(null);
    setIsLoading(false);
    isPollingRef.current = false;
  }, [stopPolling]);

  useEffect(() => {
    if (orderId) {
      startPolling();
    } else {
      stopPolling();
    }

    return () => {
      stopPolling();
    };
  }, [orderId]); // Remove startPolling from dependencies to prevent infinite loop

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
