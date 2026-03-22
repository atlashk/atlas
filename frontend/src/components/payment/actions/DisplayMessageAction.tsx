"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { AlertCircle, CheckCircle, Info, XCircle } from "lucide-react";

interface DisplayMessageActionProps {
  message: string;
  orderId: string;
  messageType?: "info" | "success" | "warning" | "error";
  onClose?: () => void;
}

export function DisplayMessageAction({
  message,
  orderId,
  messageType = "info",
  onClose,
}: DisplayMessageActionProps) {
  const getIcon = () => {
    switch (messageType) {
      case "success":
        return <CheckCircle className="h-12 w-12 text-green-600" />;
      case "warning":
        return <AlertCircle className="h-12 w-12 text-yellow-600" />;
      case "error":
        return <XCircle className="h-12 w-12 text-red-600" />;
      default:
        return <Info className="h-12 w-12 text-blue-600" />;
    }
  };

  const getTitle = () => {
    switch (messageType) {
      case "success":
        return "Payment Successful";
      case "warning":
        return "Payment Warning";
      case "error":
        return "Payment Error";
      default:
        return "Payment Information";
    }
  };

  const getTextColor = () => {
    switch (messageType) {
      case "success":
        return "text-green-600";
      case "warning":
        return "text-yellow-600";
      case "error":
        return "text-red-600";
      default:
        return "text-blue-600";
    }
  };

  return (
    <Card>
      <CardContent className="p-6">
        <div className="text-center space-y-4">
          <div className="flex justify-center">{getIcon()}</div>

          <div>
            <h3 className={`text-lg font-semibold ${getTextColor()}`}>
              {getTitle()}
            </h3>
            <p className="text-gray-600 mt-2 whitespace-pre-wrap">
              {message || "No additional information available."}
            </p>
            <p className="text-sm text-gray-500 mt-4">Order ID: {orderId}</p>
          </div>

          {onClose && (
            <Button onClick={onClose} className="w-full" size="lg">
              Continue
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
