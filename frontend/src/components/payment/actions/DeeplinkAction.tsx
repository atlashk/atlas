"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { CreditCard, ExternalLink } from "lucide-react";
import { useState } from "react";

interface DeeplinkActionProps {
  deeplinkUrl: string;
  orderId: string;
  appName?: string;
  onError?: (error: string) => void;
}

export function DeeplinkAction({
  deeplinkUrl,
  orderId,
  appName = "Payment App",
  onError,
}: DeeplinkActionProps) {
  const [isOpening, setIsOpening] = useState(false);

  const handleOpenApp = async () => {
    if (!deeplinkUrl) {
      onError?.("No deeplink URL provided");
      return;
    }

    try {
      setIsOpening(true);

      // Try to open the deeplink
      window.location.href = deeplinkUrl;

      // Reset the loading state after a delay
      setTimeout(() => {
        setIsOpening(false);
      }, 3000);
    } catch (error) {
      console.error("Deeplink error:", error);
      onError?.("Failed to open payment app");
      setIsOpening(false);
    }
  };

  const handleFallbackRedirect = () => {
    try {
      // If deeplink fails, try opening in new tab
      window.open(deeplinkUrl, "_blank");
    } catch (error) {
      console.error("Fallback redirect error:", error);
      onError?.("Failed to open payment page");
    }
  };

  if (!deeplinkUrl) {
    return (
      <Card>
        <CardContent className="p-6">
          <div className="text-center space-y-4">
            <CreditCard className="w-12 h-12 mx-auto text-red-500" />
            <h3 className="text-lg font-semibold text-red-600">
              Configuration Error
            </h3>
            <p className="text-gray-600">
              No deeplink URL was provided for this payment.
            </p>
            <p className="text-sm text-gray-500">Order ID: {orderId}</p>
            <Button
              onClick={() => onError?.("No deeplink URL provided")}
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
          <CreditCard className="w-12 h-12 mx-auto text-blue-500" />
          <h3 className="text-lg font-semibold">Open {appName}</h3>
          <p className="text-gray-600">Complete your payment using {appName}</p>
          <p className="text-sm text-gray-500">Order ID: {orderId}</p>

          <div className="space-y-3">
            <Button
              onClick={handleOpenApp}
              disabled={isOpening}
              className="w-full"
              size="lg"
            >
              {isOpening ? (
                "Opening App..."
              ) : (
                <>
                  <CreditCard className="w-4 h-4 mr-2" />
                  Open {appName}
                </>
              )}
            </Button>

            <div className="text-sm text-gray-500">
              <p>App not opening?</p>
              <Button
                onClick={handleFallbackRedirect}
                variant="link"
                size="sm"
                className="p-0 h-auto"
              >
                <ExternalLink className="w-3 h-3 mr-1" />
                Try opening in browser
              </Button>
            </div>
          </div>

          <div className="space-y-2 text-xs text-gray-500">
            <p>This will open {appName} to complete the transaction</p>
            <p>Make sure you have {appName} installed on your device</p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
