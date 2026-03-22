"use client";

import { Card, CardContent } from "@/components/ui/card";
import { QrCode } from "lucide-react";
import { useEffect, useState } from "react";

interface QrCodeActionProps {
  qrCodeData?: string;
  orderId: string;
  expirationTime?: number; // in minutes
}

export function QrCodeAction({
  qrCodeData,
  orderId,
  expirationTime = 10,
}: QrCodeActionProps) {
  const [timeLeft, setTimeLeft] = useState(expirationTime * 60); // Convert to seconds

  useEffect(() => {
    if (timeLeft <= 0) return;

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft]);

  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
  };

  return (
    <Card>
      <CardContent className="p-6">
        <div className="text-center space-y-4">
          <QrCode className="w-12 h-12 mx-auto text-blue-500" />
          <h3 className="text-lg font-semibold">Scan QR Code to Pay</h3>
          <p className="text-gray-600">
            Scan the QR code with your mobile banking app or
            &ldquo;e-wallet&rdquo; to complete the payment.
          </p>
          <p className="text-sm text-gray-500">Order ID: {orderId}</p>

          {/* QR Code Display */}
          <div className="bg-gray-100 p-8 rounded-lg border-2 border-dashed border-gray-300 mx-auto max-w-xs">
            <div className="aspect-square bg-white rounded flex items-center justify-center">
              {qrCodeData ? (
                // In a real implementation, you would use a QR code library here
                // For now, showing a placeholder
                <div className="text-xs text-gray-500 text-center p-4">
                  QR Code
                  <br />
                  {qrCodeData.substring(0, 20)}...
                </div>
              ) : (
                <QrCode className="w-24 h-24 text-gray-400" />
              )}
            </div>
          </div>

          {/* Timer */}
          {timeLeft > 0 ? (
            <div className="text-sm">
              <span className="text-gray-600">Expires in: </span>
              <span className="font-mono font-semibold text-red-600">
                {formatTime(timeLeft)}
              </span>
            </div>
          ) : (
            <div className="text-sm text-red-600 font-semibold">
              QR Code has expired
            </div>
          )}

          {/* Instructions */}
          <div className="space-y-2">
            <p className="text-sm font-medium">Instructions:</p>
            <ol className="text-sm text-gray-600 text-left space-y-1">
              <li>1. Open your mobile banking app</li>
              <li>
                2. Select &ldquo;Scan QR Code&rdquo; or &ldquo;Pay by QR&rdquo;
              </li>
              <li>3. Point your camera at the QR code above</li>
              <li>4. Confirm the payment amount</li>
              <li>5. Complete the payment</li>
            </ol>
          </div>

          <p className="text-xs text-gray-500">
            The payment will be processed automatically once completed
          </p>
        </div>
      </CardContent>
    </Card>
  );
}
