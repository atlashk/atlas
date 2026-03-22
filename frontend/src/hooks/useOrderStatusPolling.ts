import { useState, useEffect, useRef, useCallback } from "react";
import { orderApi } from "@/api/order.api";
import { OrderStatusResponse } from "@/interfaces/order.interface";

const POLLING_INTERVAL_MS = 2000;
const MAX_POLLING_DURATION_MS = 15 * 60 * 1000;

export function useOrderStatusPolling(orderId: string | null) {
  const [orderStatus, setOrderStatus] = useState<OrderStatusResponse | null>(
    null,
  );
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);
  const isPollingRef = useRef<boolean>(false);
  const orderIdRef = useRef<string | null>(orderId);
  const orderStatusRef = useRef<OrderStatusResponse | null>(null);

  useEffect(() => {
    orderIdRef.current = orderId;
  }, [orderId]);

  useEffect(() => {
    orderStatusRef.current = orderStatus;
  }, [orderStatus]);

  const stopPolling = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
    isPollingRef.current = false;
  }, []);

  const poll = useCallback(async () => {
    const currentOrderId = orderIdRef.current;
    if (!currentOrderId) return;

    try {
      const response = await orderApi.retrieveOrderStatus(currentOrderId);

      if (response.success && response.data) {
        orderStatusRef.current = response.data;
        setOrderStatus(response.data);

        if (["FULFILLED", "CANCELED"].includes(response.data.status)) {
          setIsLoading(false);
          stopPolling();
          return;
        }
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unknown error");
      setIsLoading(false);
      stopPolling();
    }
  }, [stopPolling]);

  const schedulePolling = useCallback(() => {
    if (!orderIdRef.current) return;

    if (isPollingRef.current) {
      return;
    }

    stopPolling();
    isPollingRef.current = true;
    timeoutRef.current = setTimeout(() => {
      if (orderStatusRef.current?.status !== "FULFILLED") {
        setError(
          "Order processing timed out after 15 minutes. Please try again or check your order history.",
        );
        setIsLoading(false);
        stopPolling();
      }
    }, MAX_POLLING_DURATION_MS);

    setTimeout(() => {
      setIsLoading(true);
      setError(null);
      poll();
      intervalRef.current = setInterval(poll, POLLING_INTERVAL_MS);
    }, 0);
  }, [poll, stopPolling]);

  const startPolling = useCallback(() => {
    schedulePolling();
  }, [schedulePolling]);

  const reset = useCallback(() => {
    stopPolling();
    orderStatusRef.current = null;
    setOrderStatus(null);
    setError(null);
    setIsLoading(false);
    isPollingRef.current = false;
  }, [stopPolling]);

  useEffect(() => {
    if (orderId) {
      schedulePolling();
    } else {
      stopPolling();
    }

    return () => {
      stopPolling();
    };
  }, [orderId, schedulePolling, stopPolling]);

  // Helper function to check if payment processing is needed
  const needsPaymentProcessing =
    orderStatus?.status === "AWAITING_PAYMENT_PROCESSED";

  return {
    orderStatus,
    isLoading,
    error,
    needsPaymentProcessing,
    startPolling,
    stopPolling,
    reset,
  };
}
