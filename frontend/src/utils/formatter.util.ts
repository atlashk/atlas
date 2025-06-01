import { OrderStatus } from "@/interfaces/order.interface";
import { ProductStatus } from "@/interfaces/product.interface";

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
      return 'badge bg-danger';
    default:
      return 'badge bg-primary';
  }
}

export const getProductStatusBadgeClasses = (status: ProductStatus): string => {
  if (!status) {
    return 'badge bg-primary';
  }
  switch (status) {
    case ProductStatus.IN_STOCK:
      return 'badge bg-success';
    case ProductStatus.OUT_STOCK:
      return 'badge bg-danger';
    case ProductStatus.DISCONTINUED:
      return 'badge bg-warning text-dark';
    default:
      return 'badge bg-primary';
  }
};

export const formatProductStatusLabel = (status: ProductStatus): string => {
  return status.split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
};

export const getOrderStatusBadgeClasses = (status: OrderStatus): string => {
  if (!status) {
    return 'badge bg-primary';
  }
  switch (status.toUpperCase()) {
    case 'PROCESSING':
      return 'badge bg-warning text-dark';
    case 'CONFIRMED':
      return 'badge bg-success';
    case 'CANCELED':
      return 'badge bg-danger';
    default:
      return 'badge bg-primary';
  }
}

export const formatOrderStatusLabel = (status: OrderStatus): string => {
  return status
    .split('_')
    .map(word => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
};
