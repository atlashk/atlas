import { ListOrderFilters, Order, OrderStatus } from '@/interfaces';
import { orderService } from '@/services';
import { formatCurrency, formatDate, formatOrderStatusLabel, getOrderStatusBadgeClasses } from '@/utils/formatter.util';
import React, { useCallback, useEffect, useState } from 'react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Search, RotateCcw, ChevronDown, ChevronUp } from 'lucide-react';
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from '@/components/ui/pagination';

interface Metadata {
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalRecords: number;
}

const OrderHistory: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [filters, setFilters] = useState<ListOrderFilters>({
    status: '',
    startDate: undefined,
    endDate: undefined,
    page: 1,
    size: 20,
  });
  const [metadata, setMetadata] = useState<Metadata>({
    currentPage: 1,
    pageSize: 20,
    totalPages: 1,
    totalRecords: 0,
  });

  // Get all order statuses for the dropdown
  const orderStatuses = Object.values(OrderStatus);

  const applyFilters = useCallback(async (page: number) => {
    if (isLoading) return;

    setIsLoading(true);
    try {
      const updatedFilters = { ...filters, page };
      setMetadata(prev => ({ ...prev, currentPage: page }));

      // Clean up empty or undefined filters
      const apiFilters: ListOrderFilters = { ...updatedFilters };
      Object.keys(apiFilters).forEach((key) => {
        const typedKey = key as keyof ListOrderFilters;
        if (apiFilters[typedKey] === '' || apiFilters[typedKey] === undefined) {
          delete apiFilters[typedKey];
        }
      });

      const response = await orderService.listOrder(apiFilters);
      if (response.success) {
        setOrders(response.data || []);
        if (response.metadata) {
          setMetadata(response.metadata);
        }
      } else {
        toast.error(response.errorMessage || 'Failed to load orders');
      }
    } catch (error) {
      console.error('Error loading orders:', error);
      toast.error('Failed to load orders');
    } finally {
      setIsLoading(false);
    }
  }, [filters, isLoading]);

  const changePage = (newPage: number) => {
    if (newPage >= 1 && newPage <= metadata.totalPages) {
      applyFilters(newPage);
    }
  };

  const resetFilters = () => {
    const resetFilters: ListOrderFilters = {
      status: '' as const,
      startDate: undefined,
      endDate: undefined,
      page: 1,
      size: 20,
    };
    setFilters(resetFilters);
    applyFilters(1);
  };

  const toggleDetails = (orderId: number) => {
    setSelectedOrderId(selectedOrderId === orderId ? null : orderId);
  };

  const handleFilterChange = (field: keyof ListOrderFilters, value: string | number | boolean | OrderStatus | undefined) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    applyFilters(1);
  };

  useEffect(() => {
    applyFilters(1);
  }, [applyFilters]);

  return (
    <div className="bg-white p-6 border border-gray-200 shadow-sm rounded-lg mb-6">
      {/* Filters Section */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Filter Orders</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-2">
                  Start Date
                </label>
                <input
                  id="startDate"
                  type="date"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  value={filters.startDate || ''}
                  onChange={(e) => handleFilterChange('startDate', e.target.value || undefined)}
                />
              </div>
              <div>
                <label htmlFor="endDate" className="block text-sm font-medium text-gray-700 mb-2">
                  End Date
                </label>
                <input
                  id="endDate"
                  type="date"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  value={filters.endDate || ''}
                  onChange={(e) => handleFilterChange('endDate', e.target.value || undefined)}
                />
              </div>
              <div>
                <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-2">
                  Status
                </label>
                <select
                  id="status"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  value={filters.status || ''}
                  onChange={(e) => handleFilterChange('status', e.target.value)}
                >
                  <option value="">All Statuses</option>
                  {orderStatuses.map((status) => (
                    <option key={status} value={status}>
                      {formatOrderStatusLabel(status)}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="flex gap-2 mt-4">
              <Button type="submit" className="flex items-center gap-2">
                <Search className="h-4 w-4" />
                Search
              </Button>
              <Button type="button" variant="outline" onClick={resetFilters} className="flex items-center gap-2">
                <RotateCcw className="h-4 w-4" />
                Reset
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Loading State */}
      {isLoading && (
        <div className="flex justify-center py-8" aria-live="polite">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" role="status" aria-label="Loading orders">
            <span className="sr-only">Loading...</span>
          </div>
        </div>
      )}

      {/* No Orders */}
      {!isLoading && !orders.length && (
        <p className="text-center text-gray-500 py-8">No orders found.</p>
      )}

      {/* Orders List */}
      {!isLoading && orders.length > 0 && (
        <div className="space-y-4 mb-6">
          {orders.map((order) => (
            <Card key={order.id}>
              <CardContent className="p-6">
                <div className="flex justify-between items-start">
                  <div className="space-y-2">
                    <p className="font-semibold text-lg">Order #{order.code} - {formatDate(order.createdAt)}</p>
                    <p><span className="font-medium">Total:</span> {formatCurrency(order.amount)}</p>
                    <p className="flex items-center gap-2">
                      <span className="font-medium">Status:</span>
                      <span className={getOrderStatusBadgeClasses(order.status)}>
                        {formatOrderStatusLabel(order.status)}
                      </span>
                    </p>
                    {order.status === OrderStatus.CANCELED && order.cancelReason && (
                      <div className="mt-3">
                        <p><span className="font-medium">Cancellation Reason:</span> <span className="text-red-600">{order.cancelReason}</span></p>
                      </div>
                    )}
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => toggleDetails(order.id)}
                    className="flex items-center gap-2"
                  >
                    {selectedOrderId === order.id ? (
                      <><ChevronUp className="h-4 w-4" /> Hide Details</>
                    ) : (
                      <><ChevronDown className="h-4 w-4" /> View Details</>
                    )}
                  </Button>
                </div>

                {/* Order Details */}
                {selectedOrderId === order.id && (
                  <div className="mt-6 pt-6 border-t border-gray-200">
                    <div className="overflow-x-auto">
                      <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                          <tr>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                              Product ID
                            </th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                              Product Name
                            </th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                              Price
                            </th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                              Quantity
                            </th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                              Subtotal
                            </th>
                          </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                          {order.orderItems?.map((item) => (
                            <tr key={item.product.id}>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{item.product.id}</td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{item.product.name}</td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{formatCurrency(item.product.price)}</td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{item.quantity}</td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{formatCurrency(item.product.price * item.quantity)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Pagination */}
      {metadata.totalPages > 1 && (
        <div className="flex justify-center mt-8">
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious 
                  onClick={() => changePage(metadata.currentPage - 1)}
                  className={metadata.currentPage === 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                />
              </PaginationItem>
              
              {Array.from({ length: metadata.totalPages }, (_, i) => i + 1).map((page) => (
                <PaginationItem key={page}>
                  <PaginationLink
                    onClick={() => changePage(page)}
                    isActive={metadata.currentPage === page}
                    className="cursor-pointer"
                  >
                    {page}
                  </PaginationLink>
                </PaginationItem>
              ))}
              
              <PaginationItem>
                <PaginationNext 
                  onClick={() => changePage(metadata.currentPage + 1)}
                  className={metadata.currentPage === metadata.totalPages ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )}
    </div>
  );
};

export default OrderHistory;
