'use client';

import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useUserStore } from '../../stores/user.store';
import { useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { Clock, ChevronDown, ChevronUp, RotateCcw, Search } from 'lucide-react';
import { orderApi } from "@/api/index.api";
import { Metadata } from "@/api/apiClient";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ORDER_STATUSES } from "@/constants";
import { ListOrderFilters, Order } from "@/interfaces";
import {
  formatCurrency,
  formatDate,
  getOrderStatusBadge,
  getPaymentStatusBadge,
} from "@/utils/formatter.util";
import { toast } from "sonner";

const OrderHistoryPage: React.FC = () => {
  const { isAuthenticated, isAdmin, loading } = useUserStore();
  const [isHydrated, setIsHydrated] = useState(false);
  const router = useRouter();
  const searchParams = useSearchParams();

  // OrderHistory component state
  const isInitialized = useRef(false);
  const lastRefreshParam = useRef<string | null>(null);
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [filters, setFilters] = useState<ListOrderFilters>({
    status: undefined,
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

  useEffect(() => {
    setIsHydrated(true);
  }, []);

  // Redirect if not authenticated or is admin
  useEffect(() => {
    if (isHydrated && (!isAuthenticated() || isAdmin())) {
      router.push('/');
      return;
    }
  }, [isHydrated, isAuthenticated, isAdmin, router]);

  // OrderHistory component functions
  const applyFilters = useCallback(
    async (page: number, currentFilters?: ListOrderFilters) => {
      if (isLoading) return;

      setIsLoading(true);
      try {
        const filtersToUse = currentFilters || filters;
        const updatedFilters = {...filtersToUse, page};
        setMetadata((prev) => ({...prev, currentPage: page}));

        // Clean up empty or undefined filters for API call
        const apiFilters: ListOrderFilters = {...updatedFilters};
        Object.keys(apiFilters).forEach((key) => {
          const typedKey = key as keyof ListOrderFilters;
          if (
            apiFilters[typedKey] === "" ||
            apiFilters[typedKey] === undefined
          ) {
            delete apiFilters[typedKey];
          }
        });

        const response = await orderApi.listOrder(apiFilters);
        if (response.success) {
          setOrders(response.data || []);
          if (response.metadata) {
            setMetadata(response.metadata);
          }
        } else {
          toast.error(response.errorMessage || "Failed to load orders");
        }
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to load orders";
        toast.error(errorMessage);
      } finally {
        setIsLoading(false);
      }
    },
    [filters, isLoading]
  );

  const changePage = useCallback(
    (newPage: number) => {
      if (newPage >= 1 && newPage <= metadata.totalPages) {
        applyFilters(newPage, filters);
      }
    },
    [metadata.totalPages, applyFilters, filters]
  );

  const resetFilters = useCallback(() => {
    const resetFilters: ListOrderFilters = {
      status: undefined,
      startDate: undefined,
      endDate: undefined,
      page: 1,
      size: 20,
    };
    setFilters(resetFilters);
    applyFilters(1, resetFilters);
  }, [applyFilters]);

  const toggleDetails = useCallback(
    (orderId: number) => {
      setSelectedOrderId(selectedOrderId === orderId ? null : orderId);
    },
    [selectedOrderId]
  );

  const handleFilterChange = useCallback(
    (
      field: keyof ListOrderFilters,
      value: string | number | boolean | undefined
    ) => {
      setFilters((prev) => ({...prev, [field]: value}));
    },
    []
  );

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      applyFilters(1, filters);
    },
    [applyFilters, filters]
  );

  // Load initial data on component mount
  useEffect(() => {
    if (isInitialized.current) {
      return;
    }

    isInitialized.current = true;

    const initializeData = async () => {
      await applyFilters(1);
    };
    initializeData();
  }, [applyFilters]);

  // Listen for refresh parameter to reload data
  useEffect(() => {
    const refreshParam = searchParams.get('refresh');
    if (refreshParam && isInitialized.current && refreshParam !== lastRefreshParam.current) {
      // Only refresh if this is a new refresh parameter
      lastRefreshParam.current = refreshParam;
      applyFilters(1);
    }
  }, [searchParams]); // Remove applyFilters from dependencies to prevent continuous calls

  // Show loading while checking authentication
  if (loading || !isHydrated) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Spinner className="text-blue-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  // Don't render if user is not authenticated or is admin
  if (!isAuthenticated() || isAdmin()) {
    return null;
  }

  return (
    <div className="container mx-auto p-4">
      {/* Header */}
      <div className="flex items-center gap-4 mb-6">
        <h1 className="text-xl font-bold flex items-center gap-2">
          <Clock className="h-6 w-6" />
          Order History
        </h1>
      </div>

      {/* Filters Section */}
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>Order Filter</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label
                  htmlFor="startDate"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Start Date
                </label>
                <input
                  id="startDate"
                  type="date"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  value={filters.startDate || ""}
                  onChange={(e) =>
                    handleFilterChange("startDate", e.target.value || undefined)
                  }
                />
              </div>
              <div>
                <label
                  htmlFor="endDate"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  End Date
                </label>
                <input
                  id="endDate"
                  type="date"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  value={filters.endDate || ""}
                  onChange={(e) =>
                    handleFilterChange("endDate", e.target.value || undefined)
                  }
                />
              </div>
              <div>
                <label
                  htmlFor="status"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Status
                </label>
                <select
                  id="status"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  value={filters.status || ""}
                  onChange={(e) => handleFilterChange("status", e.target.value)}
                >
                  <option value="">All Statuses</option>
                  {ORDER_STATUSES.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="flex gap-2 mt-4">
              <Button type="submit" className="flex items-center gap-2">
                <Search className="h-4 w-4"/>
                Search
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={resetFilters}
                className="flex items-center gap-2"
              >
                <RotateCcw className="h-4 w-4"/>
                Reset
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Loading State */}
      {isLoading && (
        <div className="flex justify-center py-8" aria-live="polite">
          <Spinner 
            className="text-blue-600"
            role="status"
            aria-label="Loading orders"
          />
        </div>
      )}

      {/* No Orders */}
      {!isLoading && !orders.length && (
        <p className="text-center text-gray-500 py-8">No orders found.</p>
      )}

      {/* Orders List */}
      {!isLoading && orders.length > 0 && (
        <Card className="mb-4">
          <CardHeader>
            <CardTitle>Order Results</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {orders.map((order) => (
                <Card key={order.id} className="my-6">
                  <CardContent>
                    <div className="flex justify-between items-start">
                      <div className="space-y-2">
                        <p className="font-semibold text-lg">
                          Order #{order.code} - {formatDate(order.createdAt)}
                        </p>
                        <p>
                          <span className="font-medium">Total:</span>{" "}
                          {formatCurrency(order.amount)}
                        </p>
                        <p className="flex items-center gap-2">
                          <span className="font-medium">Status:</span>
                          {getOrderStatusBadge(order.status)}
                        </p>
                        {order.status === "CANCELED" && order.cancellationReason && (
                          <div className="mt-2">
                            <p>
                              <span className="font-medium">
                                Cancellation Reason:
                              </span>{" "}
                              <span className="text-red-600">
                                {order.cancellationReason}
                              </span>
                            </p>
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
                          <>
                            <ChevronUp className="h-4 w-4"/> Hide Details
                          </>
                        ) : (
                          <>
                            <ChevronDown className="h-4 w-4"/> View Details
                          </>
                        )}
                      </Button>
                    </div>

                    {/* Order Details */}
                    {selectedOrderId === order.id && (
                      <div className="mt-4 pt-4 border-t border-gray-200">
                        {/* Order Items Table */}
                        <div className="mb-6">
                          <h4 className="text-lg font-semibold mb-3">Order Items</h4>
                          <Table>
                            <TableHeader>
                              <TableRow>
                                <TableHead>Product ID</TableHead>
                                <TableHead>Product Name</TableHead>
                                <TableHead>Price</TableHead>
                                <TableHead>Quantity</TableHead>
                                <TableHead>Subtotal</TableHead>
                              </TableRow>
                            </TableHeader>
                            <TableBody>
                              {order.orderItems?.map((item) => (
                                <TableRow key={item.product.id}>
                                  <TableCell>{item.product.id}</TableCell>
                                  <TableCell>{item.product.name}</TableCell>
                                  <TableCell>
                                    {formatCurrency(item.product.price)}
                                  </TableCell>
                                  <TableCell>{item.quantity}</TableCell>
                                  <TableCell>
                                    {formatCurrency(
                                      item.product.price * item.quantity
                                    )}
                                  </TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        </div>

                        {/* Payment Information */}
                        {order.payment && (
                          <div className="mt-6 pt-4 border-t border-gray-200">
                            <h4 className="text-lg font-semibold mb-3">Payment Information</h4>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                              <div className="space-y-2">
                                {order.payment.paymentGateway && (
                                  <p>
                                    <span className="font-medium">Payment gateway:</span> {
                                      (() => {
                                        const paymentGateway = order.payment.paymentGateway;
                                        return paymentGateway ? paymentGateway.charAt(0).toUpperCase() + paymentGateway.slice(1).toLowerCase() : '';
                                      })()
                                    }
                                  </p>
                                )}
                                {order.payment.paymentMethod && (
                                  <p>
                                    <span className="font-medium">Payment method:</span> {
                                      (() => {
                                        const paymentMethod = order.payment.paymentMethod;
                                        return paymentMethod ? paymentMethod.charAt(0).toUpperCase() + paymentMethod.slice(1).toLowerCase() : '';
                                      })()
                                    }
                                  </p>
                                )}
                                {order.payment.paymentMethodDetails && (
                                  <p>
                                    <span className="font-medium">Payment details:</span> {order.payment.paymentMethodDetails}
                                  </p>
                                )}
                              </div>
                              <div className="space-y-2">
                                {order.payment.status && (
                                  <p className="flex items-center gap-2">
                                    <span className="font-medium">Status:</span>
                                    {getPaymentStatusBadge(order.payment.status)}
                                  </p>
                                )}
                                {order.payment.errorCode && (
                                  <p>
                                    <span className="font-medium">Error Code:</span> 
                                    <span className="text-red-600 ml-1">{order.payment.errorCode}</span>
                                  </p>
                                )}
                                {order.payment.errorMessage && (
                                  <p>
                                    <span className="font-medium">Error Message:</span> 
                                    <span className="text-red-600 ml-1">{order.payment.errorMessage}</span>
                                  </p>
                                )}
                                {order.payment.cancellationReason && (
                                  <p>
                                    <span className="font-medium">Cancellation Reason:</span> 
                                    <span className="text-red-600 ml-1">{order.payment.cancellationReason}</span>
                                  </p>
                                )}
                              </div>
                            </div>
                          </div>
                        )}
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Pagination */}
      {metadata.totalPages > 1 && (
        <div className="flex justify-center mt-6">
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => changePage(metadata.currentPage - 1)}
                  className={
                    metadata.currentPage === 1
                      ? "pointer-events-none opacity-50"
                      : "cursor-pointer"
                  }
                />
              </PaginationItem>

              {Array.from({length: metadata.totalPages}, (_, i) => i + 1).map(
                (page) => (
                  <PaginationItem key={page}>
                    <PaginationLink
                      onClick={() => changePage(page)}
                      isActive={metadata.currentPage === page}
                      className="cursor-pointer"
                    >
                      {page}
                    </PaginationLink>
                  </PaginationItem>
                )
              )}

              <PaginationItem>
                <PaginationNext
                  onClick={() => changePage(metadata.currentPage + 1)}
                  className={
                    metadata.currentPage === metadata.totalPages
                      ? "pointer-events-none opacity-50"
                      : "cursor-pointer"
                  }
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </div>
      )}
    </div>
  );
};

export default OrderHistoryPage;
