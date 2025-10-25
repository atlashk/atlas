"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ExternalLink, Loader2 } from "lucide-react";
import { useCallback, useEffect, useState } from "react";

interface RedirectActionProps {
  redirectUrl: string;
  orderId: string;
  onError?: (error: string) => void;
}

export function RedirectAction({
  redirectUrl,
  orderId,
  onError,
}: RedirectActionProps) {
  const [isRedirecting, setIsRedirecting] = useState(false);
  const [countdown, setCountdown] = useState(5);

  const handleRedirect = useCallback(() => {
    try {
      setIsRedirecting(true);
      window.location.href = redirectUrl;
    } catch (error) {
      console.error("Redirect error:", error);
      onError?.("Failed to redirect to payment page");
      setIsRedirecting(false);
    }
  }, [redirectUrl, onError]);

  useEffect(() => {
    const countdownInterval = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(countdownInterval);
          handleRedirect();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(countdownInterval);
  }, [handleRedirect]);

  const handleManualRedirect = () => {
    setCountdown(0);
    handleRedirect();
  };

  if (!redirectUrl) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center space-y-4">
            <h3 className="text-lg font-semibold text-red-600">
              Redirect Error
            </h3>
            <p className="text-gray-600">
              No redirect URL was provided for this payment.
            </p>
            <p className="text-sm text-gray-500">Order ID: {orderId}</p>
            <Button
              onClick={() => onError?.("No redirect URL provided")}
              variant="outline"
              className="w-full"
            >
              Go Back
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent className="p-6">
        <div className="text-center space-y-4">
          <div className="flex justify-center">
            {isRedirecting ? (
              <Loader2 className="h-12 w-12 animate-spin text-blue-600" />
            ) : (
              <ExternalLink className="h-12 w-12 text-blue-600" />
            )}
          </div>

          <div>
            <h3 className="text-lg font-semibold">
              {isRedirecting ? "Redirecting..." : "Redirecting to Payment"}
            </h3>
            <p className="text-gray-600">
              {isRedirecting
                ? "Please wait while we redirect you to the payment page."
                : `You will be redirected to complete your payment in ${countdown} seconds.`}
            </p>
            <p className="text-sm text-gray-500 mt-2">Order ID: {orderId}</p>
          </div>

          {!isRedirecting && countdown > 0 && (
            <div className="space-y-2">
              <div className="text-2xl font-bold text-blue-600">
                {countdown}
              </div>
              <Button
                onClick={handleManualRedirect}
                className="w-full"
                size="lg"
              >
                <ExternalLink className="w-4 h-4 mr-2" />
                Continue to Payment Now
              </Button>
            </div>
          )}

          {isRedirecting && (
            <p className="text-sm text-gray-500">
              If you are not redirected automatically, please check your browser settings.
            </p>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
