import { useState, useEffect, useRef, useCallback } from 'react';
import { paymentApi } from '@/api/payment.api';
import { PaymentTrackingResponse } from '@/interfaces/payment.interface';

export function usePaymentPolling(sagaId: number | null) {
  const [paymentStatus, setPaymentStatus] = useState<PaymentTrackingResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  const startPolling = useCallback(() => {
    if (!sagaId) return;

    setIsLoading(true);
    setError(null);

    const poll = async () => {
      try {
        const response = await paymentApi.getPaymentTracking(sagaId);
        
        if (response.success && response.data) {
          setPaymentStatus(response.data);

          // Stop polling if completed
          if (['SUCCEEDED', 'FAILED', 'CANCELED'].includes(response.data.status)) {
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

    // Then poll every second
    intervalRef.current = setInterval(poll, 1000);
  }, [sagaId]);

  const stopPolling = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  };

  const reset = () => {
    stopPolling();
    setPaymentStatus(null);
    setIsLoading(false);
    setError(null);
  };

  useEffect(() => {
    if (sagaId) {
      startPolling();
    }

    return () => {
      stopPolling();
    };
  }, [sagaId, startPolling]);

  return {
    paymentStatus,
    isLoading,
    error,
    startPolling,
    stopPolling,
    reset
  };
}
