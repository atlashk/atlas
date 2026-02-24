import { Badge } from "@/components/ui/badge";
import React from "react";

export const formatDate = (dateString: string) => {
  // Use a fixed timezone to ensure SSR and client render the same output
  return new Date(dateString).toLocaleString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    timeZone: "UTC",
  });
};

export const formatCurrency = (value: number): string => {
  return value.toLocaleString("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
};

export const getOrderStatusBadge = (status: string): React.ReactElement => {
  if (!status) {
    return React.createElement(Badge, { variant: "outline" }, "Unknown");
  }
  switch (status.toUpperCase()) {
    case "AWAITING_STOCK_RESERVATION":
      return React.createElement(Badge, { variant: "outline", className: "bg-yellow-600 text-white" }, "Awaiting Product Reservation");
    case "AWAITING_PAYMENT_INITIALIZED":
      return React.createElement(Badge, { variant: "outline", className: "bg-yellow-600 text-white" }, "Awaiting Payment Initialization");
    case "AWAITING_PAYMENT_PROCESSED":
      return React.createElement(Badge, { variant: "outline", className: "bg-yellow-600 text-white" }, "Awaiting Payment Processing");
    case "FULFILLED":
      return React.createElement(Badge, { variant: "default", className: "bg-green-500 text-white" }, "Fulfilled");
    case "CANCELED":
      return React.createElement(Badge, { variant: "destructive" }, "Canceled");
    default:
      return React.createElement(Badge, { variant: "outline" }, "Unknown");
  }
};
