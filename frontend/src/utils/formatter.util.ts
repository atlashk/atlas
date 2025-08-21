import { OrderStatus } from "../interfaces/order.interface";
import { ProductStatus } from "../interfaces/product.interface";

export const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export const formatCurrency = (value: number): string => {
  return value.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
};

export const getRoleBadgeClasses = (role: string | null): string => {
  if (!role) {
    return '';
  }
  switch (role.toUpperCase()) {
    case 'ADMIN':
      return 'bg-destructive text-white';
    default:
      return 'bg-primary text-primary-foreground';
  }
}

export const getProductStatusBadgeClasses = (status: ProductStatus): string => {
  if (!status) {
    return 'bg-primary text-primary-foreground';
  }
  switch (status) {
    case ProductStatus.IN_STOCK:
      return 'bg-green-500 text-white';
    case ProductStatus.OUT_STOCK:
      return 'bg-destructive text-white';
    case ProductStatus.DISCONTINUED:
      return 'bg-yellow-500 text-black';
    default:
      return 'bg-primary text-primary-foreground';
  }
};

export const formatProductStatusLabel = (status: ProductStatus): string => {
  return status.split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
};

export const getOrderStatusBadgeClasses = (status: OrderStatus): string => {
  if (!status) {
    return 'bg-primary text-primary-foreground';
  }
  switch (status.toUpperCase()) {
    case 'PROCESSING':
      return 'bg-yellow-500 text-black';
    case 'CONFIRMED':
      return 'bg-green-500 text-white';
    case 'CANCELED':
      return 'bg-destructive text-white';
    default:
      return 'bg-primary text-primary-foreground';
  }
}

export const formatOrderStatusLabel = (status: OrderStatus): string => {
  return status
    .split('_')
    .map(word => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
};