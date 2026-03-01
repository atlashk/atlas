"use client";

import { Metadata } from "@/api/apiClient";
import { orderApi } from "@/api/order.api";
import AdminLayout from "@/components/layout/AdminLayout";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { withRequireAdmin } from "@/hoc/withAuth";
import {
  type Order,
  type RetrieveOrderFilter,
} from "@/interfaces/order.interface";
import { formatCurrency, formatDate } from "@/utils/formatter.util";
import {
  ChevronDown,
  ChevronUp,
  CreditCard,
  Loader2,
  MapPin,
  RotateCcw,
  Search,
  ShoppingBag,
  TriangleAlert,
  User,
} from "lucide-react";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { toast } from "sonner";

const OrderList: React.FC = () => {
  const isInitialized = useRef(false);
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoadingOrders, setIsLoadingOrders] = useState(true);
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null);
  const [orderStatuses, setOrderStatuses] = useState<Record<string, string>>(
    {}
  );
  const [isLoadingOrderStatuses, setIsLoadingOrderStatuses] = useState(false);
  const [filters, setFilters] = useState<RetrieveOrderFilter>({
    id: undefined,
    userId: undefined,
    productId: undefined,
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
  const [dateError, setDateError] = useState<string | null>(null);

  const getDateRangeError = useCallback(
    (startDate?: string, endDate?: string) => {
      if (!startDate || !endDate) return null;
      const parsedStartDate = new Date(startDate);
      const parsedEndDate = new Date(endDate);
      if (
        Number.isNaN(parsedStartDate.getTime()) ||
        Number.isNaN(parsedEndDate.getTime())
      ) {
        return null;
      }
      return parsedStartDate > parsedEndDate
        ? "Start date cannot be after end date"
        : null;
    },
    []
  );

  const toggleDetails = useCallback(
    (orderId: string) => {
      setSelectedOrderId(selectedOrderId === orderId ? null : orderId);
    },
    [selectedOrderId]
  );

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
        error instanceof Error
          ? error.message
          : "Failed to load order statuses";
      toast.error(errorMessage);
    } finally {
      setIsLoadingOrderStatuses(false);
    }
  }, [isLoadingOrderStatuses, orderStatuses]);

  const applyFilters = useCallback(
    async (page: number, currentFilters?: RetrieveOrderFilter) => {
      setIsLoadingOrders(true);
      try {
        const filtersToUse = currentFilters || filters;
        const updatedFilters = { ...filtersToUse, page };
        setFilters(updatedFilters);
        setMetadata((prev) => ({ ...prev, currentPage: page }));

        // Clean filters for API call - remove empty or undefined values
        const apiFilters: RetrieveOrderFilter = { ...updatedFilters };
        Object.keys(apiFilters).forEach((key) => {
          const typedKey = key as keyof RetrieveOrderFilter;
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
          toast.error("Failed to load orders");
          setOrders([]);
        }
        setSelectedOrderId(null);
      } catch {
        toast.error("Failed to load orders");
        setOrders([]);
      } finally {
        setIsLoadingOrders(false);
      }
    },
    [filters]
  );

  const changePage = useCallback(
    (newPage: number) => {
      if (newPage >= 1 && newPage <= metadata.totalPages) {
        applyFilters(newPage);
      }
    },
    [metadata.totalPages, applyFilters]
  );

  const resetFilters = useCallback(() => {
    const resetFiltersData: RetrieveOrderFilter = {
      id: undefined,
      userId: undefined,
      productId: undefined,
      status: undefined,
      startDate: undefined,
      endDate: undefined,
      page: 1,
      size: 20,
    };
    setFilters(resetFiltersData);
    setDateError(null);
    applyFilters(1, resetFiltersData);
  }, [applyFilters]);

  const handleFilterChange = useCallback(
    (
      field: keyof RetrieveOrderFilter,
      value: string | number | boolean | undefined
    ) => {
      setFilters((prev) => ({ ...prev, [field]: value }));
    },
    []
  );

  const handleSearch = useCallback(() => {
    const error = getDateRangeError(filters.startDate, filters.endDate);
    if (error) {
      setDateError(error);
      toast.error(error);
      return;
    }
    applyFilters(1);
  }, [applyFilters, filters.endDate, filters.startDate, getDateRangeError]);

  // Load reference data and initial list on mount
  useEffect(() => {
    loadOrderStatuses();
  }, [loadOrderStatuses]);

  useEffect(() => {
    if (isInitialized.current) {
      return;
    }

    isInitialized.current = true;
    applyFilters(1);
  }, [applyFilters]);

  useEffect(() => {
    setDateError(getDateRangeError(filters.startDate, filters.endDate));
  }, [filters.endDate, filters.startDate, getDateRangeError]);

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Order Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="orderId">Order ID</Label>
              <Input
                type="text"
                id="orderId"
                placeholder="Enter order ID"
                value={filters.id ?? ""}
                onChange={(e) =>
                  handleFilterChange("id", e.target.value || undefined)
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="userId">User ID</Label>
              <Input
                type="text"
                id="userId"
                placeholder="Enter user ID"
                value={filters.userId ?? ""}
                onChange={(e) =>
                  handleFilterChange("userId", e.target.value || undefined)
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="productId">Product ID</Label>
              <Input
                type="text"
                id="productId"
                placeholder="Enter product ID"
                value={filters.productId ?? ""}
                onChange={(e) =>
                  handleFilterChange("productId", e.target.value || undefined)
                }
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-4">
            <div className="space-y-2">
              <Label htmlFor="startDate">Start Date</Label>
              <Input
                type="date"
                id="startDate"
                value={filters.startDate || ""}
                onChange={(e) =>
                  handleFilterChange("startDate", e.target.value || undefined)
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="endDate">End Date</Label>
              <Input
                type="date"
                id="endDate"
                value={filters.endDate || ""}
                onChange={(e) =>
                  handleFilterChange("endDate", e.target.value || undefined)
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="status">Order Status</Label>
              <Select
                value={filters.status || ""}
                onValueChange={(value) => handleFilterChange("status", value)}
                disabled={isLoadingOrderStatuses}
              >
                <SelectTrigger>
                  <SelectValue placeholder="All Statuses" />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(orderStatuses).map(
                    ([statusKey, statusLabel]) => (
                      <SelectItem key={statusKey} value={statusKey}>
                        {statusLabel}
                      </SelectItem>
                    )
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>
          {dateError && (
            <p className="text-sm text-red-600 mt-2" role="alert">
              {dateError}
            </p>
          )}

          <div className="flex justify-start space-x-2 mt-4">
            <Button
              onClick={handleSearch}
              disabled={isLoadingOrders || Boolean(dateError)}
            >
              <Search className="h-4 w-4 mr-2" />
              Search
            </Button>
            <Button
              variant="outline"
              onClick={resetFilters}
              disabled={isLoadingOrders}
            >
              <RotateCcw className="h-4 w-4 mr-2" />
              Reset
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Order Results</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoadingOrders ? (
            <div className="flex flex-col items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <p className="mt-2 text-muted-foreground">Loading orders...</p>
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>User</TableHead>
                    <TableHead>Amount</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created At</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orders.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={7}
                        className="text-center py-8 text-muted-foreground"
                      >
                        No orders found
                      </TableCell>
                    </TableRow>
                  ) : (
                    orders.map((order) => (
                      <React.Fragment key={order.id}>
                        <TableRow className="hover:bg-muted/50">
                          <TableCell>{order.id}</TableCell>
                          <TableCell>
                            {order.user
                              ? `${order.user.firstName} ${order.user.lastName}`
                              : "N/A"}
                          </TableCell>
                          <TableCell>{formatCurrency(order.amount)}</TableCell>
                          <TableCell>
                            {orderStatuses[order.status] || "Unknown"}
                          </TableCell>
                          <TableCell>{formatDate(order.createdAt)}</TableCell>
                          <TableCell>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => toggleDetails(order.id)}
                            >
                              {selectedOrderId === order.id ? (
                                <>
                                  <ChevronUp className="h-4 w-4 mr-2" />
                                  Hide Details
                                </>
                              ) : (
                                <>
                                  <ChevronDown className="h-4 w-4 mr-2" />
                                  View Details
                                </>
                              )}
                            </Button>
                          </TableCell>
                        </TableRow>

                        {selectedOrderId === order.id && (
                          <TableRow>
                            <TableCell colSpan={7} className="p-0">
                              <div className="p-4 bg-muted/30 border-t">
                                <div className="space-y-5">
                                  <div className="bg-white rounded-lg border p-4 shadow-sm">
                                    <h6 className="font-semibold mb-2 border-b pb-2 flex items-center gap-2">
                                      <User className="w-4 h-4" />
                                      User Information
                                    </h6>
                                    <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                      <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                        User ID
                                      </span>
                                      {order.user?.id ?? "N/A"}
                                    </div>
                                    <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                      <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                        Full Name
                                      </span>
                                      {order.user
                                        ? `${order.user.firstName} ${order.user.lastName}`
                                        : "N/A"}
                                    </div>
                                    <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                      <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                        Email
                                      </span>
                                      {order.user?.email ?? "N/A"}
                                    </div>
                                    <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                      <span className="text-sm font-medium text-gray-600 min-w-[120px]">
                                        Phone Number
                                      </span>
                                      {order.user?.phoneNumber ?? "N/A"}
                                    </div>
                                  </div>

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

                                  {order.cancellationReason && (
                                    <div className="bg-red-50 border border-red-200 rounded-lg p-4 shadow-sm">
                                      <h6 className="font-semibold mb-2 text-red-800 border-b border-red-200 pb-2 flex items-center gap-2">
                                        <TriangleAlert className="w-4 h-4" />
                                        Cancellation Information
                                      </h6>
                                      <div className="flex items-start space-x-3">
                                        <div>
                                          <p className="text-sm text-red-800 mt-1 leading-relaxed">
                                            {order.cancellationReason}
                                          </p>
                                        </div>
                                      </div>
                                    </div>
                                  )}

                                  <div className="bg-white rounded-lg border p-4 shadow-sm">
                                    <h6 className="font-semibold mb-2 border-b pb-2 flex items-center gap-2">
                                      <ShoppingBag className="w-4 h-4" />
                                      Order Items (
                                      {order.orderItems?.length || 0} items)
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
                                          {order.orderItems?.map(
                                            (item, index) => (
                                              <TableRow
                                                key={item.product.id}
                                                className={
                                                  index % 2 === 0
                                                    ? "bg-white"
                                                    : "bg-gray-50/50"
                                                }
                                              >
                                                <TableCell className="text-sm text-gray-600">
                                                  {item.product.id}
                                                </TableCell>
                                                <TableCell className="font-medium text-gray-900">
                                                  {item.product.name}
                                                </TableCell>
                                                <TableCell className="text-gray-700">
                                                  {formatCurrency(
                                                    item.product.price
                                                  )}
                                                </TableCell>
                                                <TableCell className="text-center">
                                                  {item.quantity}
                                                </TableCell>
                                                <TableCell className="font-semibold text-gray-900">
                                                  {formatCurrency(
                                                    item.product.price *
                                                      item.quantity
                                                  )}
                                                </TableCell>
                                              </TableRow>
                                            )
                                          )}
                                        </TableBody>
                                      </Table>
                                    </div>

                                    <div className="mt-6 pt-4 border-t border-gray-200">
                                      <div className="flex justify-between items-center">
                                        <span className="font-semibold">
                                          Total Amount:
                                        </span>
                                        <span className="font-bold text-green-600 bg-green-50 px-4 py-2 rounded-lg">
                                          {formatCurrency(order.amount)}
                                        </span>
                                      </div>
                                    </div>
                                  </div>

                                  {order.payment && (
                                    <div className="bg-white rounded-lg border p-4 shadow-sm">
                                      <h6 className="font-semibold mb-2 border-b pb-2 flex items-center gap-2">
                                        <CreditCard className="w-4 h-4" />
                                        Payment Information
                                      </h6>
                                      {order.payment.paymentGatewayName && (
                                        <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                          <span className="text-sm font-medium text-gray-600 min-w-[140px]">
                                            Payment Gateway
                                          </span>
                                          <span className="bg-blue-50 text-blue-700 px-3 py-1 rounded-full">
                                            {(() => {
                                              const paymentGatewayName =
                                                order.payment
                                                  .paymentGatewayName;
                                              return paymentGatewayName
                                                ? paymentGatewayName
                                                    .charAt(0)
                                                    .toUpperCase() +
                                                    paymentGatewayName
                                                      .slice(1)
                                                      .toLowerCase()
                                                : "";
                                            })()}
                                          </span>
                                        </div>
                                      )}
                                      {order.payment.paymentMethod && (
                                        <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                          <span className="text-sm font-medium text-gray-600 min-w-[140px]">
                                            Payment Method
                                          </span>
                                          {(() => {
                                            const paymentMethod =
                                              order.payment.paymentMethod;
                                            return paymentMethod
                                              ? paymentMethod
                                                  .charAt(0)
                                                  .toUpperCase() +
                                                  paymentMethod
                                                    .slice(1)
                                                    .toLowerCase()
                                              : "";
                                          })()}
                                        </div>
                                      )}
                                      {order.payment.paymentMethodDetails && (
                                        <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                          <span className="text-sm font-medium text-gray-600 min-w-[140px]">
                                            Payment Details
                                          </span>
                                          {order.payment.paymentMethodDetails}
                                        </div>
                                      )}
                                      {order.payment.transactionId && (
                                        <div className="flex items-center justify-between py-2 border-b border-gray-100 last:border-b-0">
                                          <span className="text-sm font-medium text-gray-600 min-w-[140px]">
                                            Transaction ID
                                          </span>
                                          {order.payment.transactionId}
                                        </div>
                                      )}
                                    </div>
                                  )}
                                </div>
                              </div>
                            </TableCell>
                          </TableRow>
                        )}
                      </React.Fragment>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {metadata.totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            Page {metadata.currentPage} of {metadata.totalPages} (
            {metadata.totalRecords} records)
          </div>
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    if (metadata.currentPage > 1) {
                      changePage(metadata.currentPage - 1);
                    }
                  }}
                  className={
                    metadata.currentPage <= 1
                      ? "pointer-events-none opacity-50"
                      : ""
                  }
                />
              </PaginationItem>

              {Array.from(
                { length: Math.min(5, metadata.totalPages) },
                (_, i) => {
                  const pageNumber =
                    Math.max(
                      1,
                      Math.min(
                        metadata.totalPages - 4,
                        metadata.currentPage - 2
                      )
                    ) + i;

                  if (pageNumber <= metadata.totalPages) {
                    return (
                      <PaginationItem key={pageNumber}>
                        <PaginationLink
                          href="#"
                          onClick={(e) => {
                            e.preventDefault();
                            changePage(pageNumber);
                          }}
                          isActive={pageNumber === metadata.currentPage}
                        >
                          {pageNumber}
                        </PaginationLink>
                      </PaginationItem>
                    );
                  }
                  return null;
                }
              )}

              <PaginationItem>
                <PaginationNext
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    if (metadata.currentPage < metadata.totalPages) {
                      changePage(metadata.currentPage + 1);
                    }
                  }}
                  className={
                    metadata.currentPage >= metadata.totalPages
                      ? "pointer-events-none opacity-50"
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

function AdminOrderListPage() {
  return (
    <AdminLayout>
      <OrderList />
    </AdminLayout>
  );
}

export default withRequireAdmin(AdminOrderListPage);
