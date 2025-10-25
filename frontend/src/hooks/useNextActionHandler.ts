"use client";

import { PaymentNextAction } from "@/interfaces/payment.interface";
import { useCallback, useEffect, useState } from "react";

interface UseNextActionHandlerProps {
  nextAction: PaymentNextAction | null;
  orderId: string;
  onPaymentComplete?: () => void;
  onPaymentError?: (error: string) => void;
}

export function useNextActionHandler({
  nextAction,
  orderId,
  onPaymentComplete,
  onPaymentError,
}: UseNextActionHandlerProps) {
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Reset error when nextAction changes
  useEffect(() => {
    setError(null);
    setIsProcessing(false);
  }, [nextAction]);

  const handlePaymentComplete = useCallback(() => {
    setIsProcessing(false);
    setError(null);
    onPaymentComplete?.();
  }, [onPaymentComplete]);

  const handlePaymentError = useCallback(
    (errorMessage: string) => {
      setIsProcessing(false);
      setError(errorMessage);
      onPaymentError?.(errorMessage);
    },
    [onPaymentError]
  );

  const handleRetry = useCallback(() => {
    setError(null);
    setIsProcessing(false);
  }, []);

  const startProcessing = useCallback(() => {
    setIsProcessing(true);
    setError(null);
  }, []);

  // Validate nextAction
  const isValidNextAction = useCallback(() => {
    if (!nextAction) return false;
    if (!orderId) return false;

    switch (nextAction.type) {
      case "USE_PAYMENT_ELEMENT":
        return !!(
          nextAction.clientSecret &&
          nextAction.publishableKey &&
          nextAction.provider
        );
      case "REDIRECT_URL":
        return !!nextAction.url;
      case "DEEPLINK":
        return !!nextAction.url;
      case "QR_CODE":
        return !!nextAction.content;
      default:
        return false;
    }
  }, [nextAction, orderId]);

  return {
    isProcessing,
    error,
    isValidNextAction: isValidNextAction(),
    handlePaymentComplete,
    handlePaymentError,
    handleRetry,
    startProcessing,
  };
}
