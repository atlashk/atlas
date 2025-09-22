import { Badge } from "@/components/ui/badge";
import { PaymentStatus } from "@/interfaces/order.interface";
import React from "react";

export const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export const formatCurrency = (value: number): string => {
  return value.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
};

export const getRoleBadgeClasses = (role: string | null): string => {
  if (!role) {
    return "";
  }
  switch (role.toUpperCase()) {
    case "ADMIN":
      return "bg-destructive text-white";
    default:
      return "bg-primary text-primary-foreground";
  }
};

export const getProductStatusBadge = (status: string): React.ReactElement => {
  if (!status) {
    return React.createElement(Badge, { variant: "outline" }, "Unknown");
  }
  switch (status) {
    case "IN_STOCK":
      return React.createElement(Badge, { variant: "default" }, "In Stock");
    case "OUT_STOCK":
      return React.createElement(
        Badge,
        { variant: "destructive" },
        "Out of Stock"
      );
    case "DISCONTINUED":
      return React.createElement(
        Badge,
        { variant: "outline", className: "bg-yellow-500 text-black" },
        "Discontinued"
      );
    default:
      return React.createElement(Badge, { variant: "outline" }, "Unknown");
  }
};

export const getOrderStatusBadge = (status: string): React.ReactElement => {
  if (!status) {
    return React.createElement(Badge, { variant: "outline" }, "Unknown");
  }
  switch (status.toUpperCase()) {
    case "PROCESSING":
      return React.createElement(Badge, { variant: "outline" }, "Processing");
    case "CONFIRMED":
      return React.createElement(Badge, { variant: "default" }, "Confirmed");
    case "CANCELED":
    case "CANCELLED":
      return React.createElement(Badge, { variant: "destructive" }, "Canceled");
    default:
      return React.createElement(Badge, { variant: "outline" }, "Unknown");
  }
};

export const getPaymentStatusBadge = (status: PaymentStatus): React.ReactElement => {
  switch (status) {
    case PaymentStatus.SUCCEEDED:
      return React.createElement(Badge, { variant: "default", className: "bg-green-500 text-white" }, "Succeeded");
    case PaymentStatus.CREATED:
      return React.createElement(Badge, { variant: "outline", className: "bg-blue-500 text-white" }, "Created");
    case PaymentStatus.CANCELED:
      return React.createElement(Badge, { variant: "destructive" }, "Canceled");
    case PaymentStatus.FAILED:
      return React.createElement(Badge, { variant: "destructive", className: "bg-red-600 text-white" }, "Failed");
    case PaymentStatus.UNKNOWN:
    default:
      return React.createElement(Badge, { variant: "outline" }, "Unknown");
  }
};
