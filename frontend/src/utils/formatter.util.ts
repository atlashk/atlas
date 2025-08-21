import { Badge } from "@/components/ui/badge";
import React from "react";
import { OrderStatus } from "../interfaces/order.interface";
import { ProductStatus } from "../interfaces/product.interface";

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

export const getProductStatusBadge = (
  status: ProductStatus
): React.ReactElement => {
  if (!status) {
    return React.createElement(Badge, { variant: "outline" }, "Unknown");
  }
  switch (status) {
    case ProductStatus.IN_STOCK:
      return React.createElement(Badge, { variant: "default" }, "In Stock");
    case ProductStatus.OUT_STOCK:
      return React.createElement(
        Badge,
        { variant: "destructive" },
        "Out of Stock"
      );
    case ProductStatus.DISCONTINUED:
      return React.createElement(
        Badge,
        { variant: "outline", className: "bg-yellow-500 text-black" },
        "Discontinued"
      );
    default:
      return React.createElement(Badge, { variant: "outline" }, "Unknown");
  }
};

export const getOrderStatusBadge = (
  status: OrderStatus
): React.ReactElement => {
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
