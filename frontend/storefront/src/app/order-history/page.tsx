"use client";

import { Metadata } from "@/api/apiClient";
import { orderApi } from "@/api/index.api";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { Spinner } from "@/components/ui/spinner";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { withAuth } from "@/hoc/withAuth";
import { Order, RetrieveOrderListFilter } from "@/interfaces";
import {
  formatCurrency,
  formatDate,
  getOrderStatusBadge,
} from "@/utils/formatter.util";
import {
  ChevronDown,
  ChevronUp,
  Clock,
  CreditCard,
  MapPin,
  RotateCcw,
  Search,
  ShoppingBag,
} from "lucide-react";
import React, {
  Suspense,
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";
import { toast } from "sonner";

const OrderHistoryContent: React.FC = () => {
  // OrderHistory component state
  const isInitialized = useRef(false);
  const lastRefreshParam = useRef<string | null>(null);
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isClient, setIsClient] = useState(false);
  const [orderStatuses, setOrderStatuses] = useState<Record<string, string>>({});
  const [isLoadingOrderStatuses, setIsLoadingOrderStatuses] = useState(false);
  const [filters, setFilters] = useState<RetrieveOrderListFilter>({
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

  // OrderHistory component functions
  const loadOrderStatuses = useCallback(async () => {
    if (isLoadingOrderStatuses || Object.keys(orderStatuses).length > 0) return;

    setIsLoadingOrderStatuses(true);
    try {
      const response = await orderApi.retrieveOrderStatuses();
      if (response.success && response.data) {
        setOrderStatuses(response.data);
      } else {
        toast.error(response.errorMessage || "Failed to load order statuses");
      }
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to load order statuses";
      toast.error(errorMessage);
    } finally {
      setIsLoadingOrderStatuses(false);
    }
  }, [isLoadingOrderStatuses, orderStatuses]);

  const applyFilters = useCallback(
    async (page: number, currentFilters?: RetrieveOrderListFilter) => {
      // Prevent multiple simultaneous API calls
      if (isLoading || !isClient) return;

      setIsLoading(true);
      try {
        const filtersToUse = currentFilters || filters;
        const updatedFilters = { ...filtersToUse, page };
        setMetadata((prev) => ({ ...prev, currentPage: page }));

        // Clean up empty or undefined filters for API call
        const apiFilters: RetrieveOrderListFilter = { ...updatedFilters };
        Object.keys(apiFilters).forEach((key) => {
          const typedKey = key as keyof RetrieveOrderListFilter;
          if (
            apiFilters[typedKey] === "" ||
            apiFilters[typedKey] === undefined
          ) {
            delete apiFilters[typedKey];
          }
        });

        const response = await orderApi.retrieveOrderList(apiFilters);
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
    [filters, isLoading, isClient]
  );

  const changePage = useCallback(
    (newPage: number) => {
      if (newPage >= 1 && newPage <= metadata.totalPages && !isLoading) {
        applyFilters(newPage, filters);
      }
    },
    [metadata.totalPages, applyFilters, filters, isLoading]
  );

  const resetFilters = useCallback(() => {
    if (isLoading) return;

    const resetFilters: RetrieveOrderListFilter = {
      status: undefined,
      startDate: undefined,
      endDate: undefined,
      page: 1,
      size: 20,
    };
    setFilters(resetFilters);
    applyFilters(1, resetFilters);
  }, [applyFilters, isLoading]);

  const toggleDetails = useCallback(
    (orderId: string) => {
      if (isLoading) return;
      setSelectedOrderId(selectedOrderId === orderId ? null : orderId);
    },
    [selectedOrderId, isLoading]
  );

  const handleFilterChange = useCallback(
    (
      field: keyof RetrieveOrderListFilter,
      value: string | number | boolean | undefined
    ) => {
      setFilters((prev) => ({ ...prev, [field]: value }));
    },
    []
  );

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      if (isLoading) return;
      applyFilters(1, filters);
    },
    [applyFilters, filters, isLoading]
  );

  // Keep a stable reference to applyFilters to avoid changing effect dependencies size
  const applyFiltersRef = useRef(applyFilters);
  useEffect(() => {
    applyFiltersRef.current = applyFilters;
  }, [applyFilters]);

  // Guard to prevent duplicate initial fetch
  const hasFetchedRef = useRef(false);

  // Set isClient to true on mount to prevent hydration issues
  useEffect(() => {
    setIsClient(true);
  }, []);

  // Load order statuses on mount
  useEffect(() => {
    if (isClient) {
      loadOrderStatuses();
    }
  }, [isClient, loadOrderStatuses]);

  // Load initial data only after client hydration to avoid early exit
  useEffect(() => {
    if (isInitialized.current || !isClient || hasFetchedRef.current) {
      return;
    }

    isInitialized.current = true;
    // If a refresh param is present on first load, record it to avoid a second call
    if (typeof window !== 'undefined') {
      const urlParams = new URLSearchParams(window.location.search);
      const initialRefresh = urlParams.get("refresh");
      if (initialRefresh) {
        lastRefreshParam.current = initialRefresh;
      }
    }

    hasFetchedRef.current = true;
    applyFiltersRef.current(1);
  }, [isClient]);

  // Listen for refresh parameter changes to reload data after first fetch
  useEffect(() => {
    if (!isInitialized.current || !isClient || typeof window === 'undefined') return;
    
    // Use window.location.search to avoid hydration issues with useSearchParams
    const urlParams = new URLSearchParams(window.location.search);
    const refreshParam = urlParams.get("refresh");
    if (refreshParam && refreshParam !== lastRefreshParam.current) {
      // Only refresh if this is a new refresh parameter
      lastRefreshParam.current = refreshParam;
      applyFiltersRef.current(1);
    }
  }, [isClient]); // Only run when isClient is true

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
                  disabled={isLoadingOrderStatuses}
                >
                  <option value="">All Statuses</option>
                  {Object.entries(orderStatuses).map(([statusKey, statusLabel]) => (
                    <option key={statusKey} value={statusKey}>
                      {statusLabel}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="flex gap-2 mt-4">
              <Button
                type="submit"
                className="flex items-center gap-2"
                disabled={isLoading}
              >
                <Search className="h-4 w-4" />
                Search
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={resetFilters}
                className="flex items-center gap-2"
                disabled={isLoading}
              >
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
                          Order #{order.id} - {formatDate(order.createdAt)}
                        </p>
                        <p>
                          <span className="font-medium">Total:</span>{" "}
                          {formatCurrency(order.amount)}
                        </p>
                        <p className="flex items-center gap-2">
                          <span className="font-medium">Status:</span>
                          {getOrderStatusBadge(order.status)}
                        </p>
                        {order.status === "CANCELED" &&
                          order.cancellationReason && (
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
                        disabled={isLoading}
                      >
                        {selectedOrderId === order.id ? (
                          <>
                            <ChevronUp className="h-4 w-4" /> Hide Details
                          </>
                        ) : (
                          <>
                            <ChevronDown className="h-4 w-4" /> View Details
                          </>
                        )}
                      </Button>
                    </div>

                    {/* Order Details */}
                    {selectedOrderId === order.id && (
                      <div className="mt-4 space-y-5">
                        {/* Address Information */}
                        {order.address && (
                          <div className="bg-white rounded-lg border p-4 shadow-sm">
                            <h6 className="font-semibold mb-2 border-b pb-2 flex items-center gap-2">
                              <MapPin className="w-4 h-4" />
                              Delivery Address
                            </h6>
                            <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                              <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                Street
                              </span>
                              {order.address.street}
                            </div>
                            <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                              <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                City
                              </span>
                              {order.address.city}
                            </div>
                            <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                              <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                Country
                              </span>
                              {order.address.country}
                            </div>
                            <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                              <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                Postal Code
                              </span>
                              {order.address.postalCode}
                            </div>
                          </div>
                        )}

                        {/* Order Items */}
                        <div className="bg-white rounded-lg border p-4 shadow-sm">
                          <h6 className="font-semibold mb-2 border-b pb-2 flex items-center gap-2">
                            <ShoppingBag className="w-4 h-4" />
                            Order Items ({order.orderItems?.length || 0} items)
                          </h6>
                          <div className="rounded-lg border border-gray-200 overflow-hidden">
                            <Table>
                              <TableHeader>
                                <TableRow className="bg-gray-50">
                                  <TableHead className="font-semibold text-gray-700">
                                    Product ID
                                  </TableHead>
                                  <TableHead className="font-semibold text-gray-700">
                                    Product Name
                                  </TableHead>
                                  <TableHead className="font-semibold text-gray-700">
                                    Price
                                  </TableHead>
                                  <TableHead className="font-semibold text-gray-700">
                                    Quantity
                                  </TableHead>
                                  <TableHead className="font-semibold text-gray-700">
                                    Subtotal
                                  </TableHead>
                                </TableRow>
                              </TableHeader>
                              <TableBody>
                                {order.orderItems?.map((item, index) => (
                                  <TableRow
                                    key={item.product.id}
                                    className={
                                      index % 2 === 0
                                        ? "bg-white"
                                        : "bg-gray-50/50"
                                    }
                                  >
                                    <TableCell className="text-sm text-gray-700">
                                      {item.product.id}
                                    </TableCell>
                                    <TableCell className="text-sm font-medium text-gray-900">
                                      {item.product.name}
                                    </TableCell>
                                    <TableCell className="text-sm text-gray-700">
                                      {formatCurrency(item.product.price)}
                                    </TableCell>
                                    <TableCell className="text-sm text-gray-700">
                                      {item.quantity}
                                    </TableCell>
                                    <TableCell className="text-sm font-medium text-gray-900">
                                      {formatCurrency(
                                        item.product.price * item.quantity
                                      )}
                                    </TableCell>
                                  </TableRow>
                                ))}
                              </TableBody>
                            </Table>
                          </div>
                        </div>

                        {/* Payment Information */}
                        {order.payment && (
                          <div className="bg-white rounded-lg border p-4 shadow-sm">
                            <h6 className="font-semibold mb-2 border-b pb-2 flex items-center gap-2">
                              <CreditCard className="w-4 h-4" />
                              Payment Information
                            </h6>
                            {order.payment.paymentGateway && (
                              <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                  Payment Gateway
                                </span>
                                {order.payment.paymentGateway
                                  .charAt(0)
                                  .toUpperCase() +
                                  order.payment.paymentGateway
                                    .slice(1)
                                    .toLowerCase()}
                              </div>
                            )}
                            {order.payment.paymentMethod && (
                              <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                  Payment Method
                                </span>
                                {order.payment.paymentMethod
                                  .charAt(0)
                                  .toUpperCase() +
                                  order.payment.paymentMethod
                                    .slice(1)
                                    .toLowerCase()}
                              </div>
                            )}
                            {order.payment.paymentMethodDetails && (
                              <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                  Payment Details
                                </span>
                                {order.payment.paymentMethodDetails}
                              </div>
                            )}
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
                  onClick={() => {
                    if (!isLoading && metadata.currentPage > 1) {
                      changePage(metadata.currentPage - 1);
                    }
                  }}
                  className={
                    isLoading || metadata.currentPage <= 1
                      ? "pointer-events-none opacity-50 cursor-not-allowed"
                      : ""
                  }
                />
              </PaginationItem>
              {Array.from({ length: metadata.totalPages }, (_, i) => i + 1).map(
                (page) => (
                  <PaginationItem key={page}>
                    <PaginationLink
                      onClick={() => {
                        if (!isLoading) {
                          changePage(page);
                        }
                      }}
                      isActive={metadata.currentPage === page}
                      className={
                        isLoading
                          ? "pointer-events-none opacity-50 cursor-not-allowed"
                          : ""
                      }
                    >
                      {page}
                    </PaginationLink>
                  </PaginationItem>
                )
              )}
              <PaginationItem>
                <PaginationNext
                  onClick={() => {
                    if (
                      !isLoading &&
                      metadata.currentPage < metadata.totalPages
                    ) {
                      changePage(metadata.currentPage + 1);
                    }
                  }}
                  className={
                    isLoading || metadata.currentPage >= metadata.totalPages
                      ? "pointer-events-none opacity-50 cursor-not-allowed"
                      : ""
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

const OrderHistoryPageWrapper: React.FC = () => {
  return (
    <Suspense
      fallback={
        <div className="container mx-auto p-4">
          <div className="flex items-center gap-4 mb-6">
            <h1 className="text-xl font-bold flex items-center gap-2">
              <Clock className="h-6 w-6" />
              Order History
            </h1>
          </div>
          <div className="flex justify-center py-8">
            <Spinner
              className="text-blue-600"
              role="status"
              aria-label="Loading page"
            />
          </div>
        </div>
      }
    >
      <OrderHistoryContent />
    </Suspense>
  );
};

// Export with authentication HOC that requires USER role
export default withAuth(OrderHistoryPageWrapper, {
  requireAuth: true,
  allowedRoles: ["USER"],
});
