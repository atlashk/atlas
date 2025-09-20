"use client";

import {orderAdminApi} from "@/api/order.admin";
import {Metadata} from "@/api/apiClient";
import {Alert, AlertDescription} from "@/components/ui/alert";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
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
import {ORDER_STATUSES} from "@/constants";
import {
  type ListOrderFilters,
  type Order,
} from "@/interfaces/order.interface";
import {
  formatCurrency,
  formatDate,
  getOrderStatusBadge,
} from "@/utils/formatter.util";
import {
  ChevronDown,
  ChevronUp,
  Loader2,
  RotateCcw,
  Search,
} from "lucide-react";
import React, {useCallback, useEffect, useState, useRef} from "react";
import {toast} from "sonner";

const OrderList: React.FC = () => {
  const isInitialized = useRef(false);
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoadingOrders, setIsLoadingOrders] = useState(true);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [filters, setFilters] = useState<ListOrderFilters>({
    orderId: undefined,
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

  const toggleDetails = useCallback((orderId: number) => {
    setSelectedOrderId(selectedOrderId === orderId ? null : orderId);
  }, [selectedOrderId]);

  const applyFilters = useCallback(
      async (page: number, currentFilters?: ListOrderFilters) => {
        setIsLoadingOrders(true);
        try {
          const filtersToUse = currentFilters || filters;
          const updatedFilters = {...filtersToUse, page};
          setFilters(updatedFilters);
          setMetadata((prev) => ({...prev, currentPage: page}));

          // Clean filters for API call - remove empty or undefined values
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

          const response = await orderAdminApi.listOrder(apiFilters);

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
        } catch (error) {
          toast.error("Failed to load orders");
          setOrders([]);
        } finally {
          setIsLoadingOrders(false);
        }
      },
      [filters]
  );

  const changePage = useCallback((newPage: number) => {
    if (newPage >= 1 && newPage <= metadata.totalPages) {
      applyFilters(newPage);
    }
  }, [metadata.totalPages, applyFilters]);

  const resetFilters = useCallback(() => {
    const resetFiltersData: ListOrderFilters = {
      orderId: undefined,
      userId: undefined,
      productId: undefined,
      status: undefined,
      startDate: undefined,
      endDate: undefined,
      page: 1,
      size: 20,
    };
    setFilters(resetFiltersData);
    applyFilters(1, resetFiltersData);
  }, [applyFilters]);

  const handleFilterChange = useCallback((
      field: keyof ListOrderFilters,
      value: string | number | boolean | undefined
  ) => {
    setFilters((prev) => ({...prev, [field]: value}));
  }, []);

  const handleSearch = useCallback(() => {
    applyFilters(1);
  }, [applyFilters]);

  // Load initial data on component mount
  useEffect(() => {
    if (isInitialized.current) {
      return;
    }

    isInitialized.current = true;
    applyFilters(1);
  }, []);

  return (
      <div className="space-y-6">
        {/* Filters Card */}
        <Card>
          <CardHeader>
            <CardTitle>Order Filters</CardTitle>
          </CardHeader>
          <CardContent>
            {/* Row 1: Order ID, User ID, Product ID */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="orderId">Order ID</Label>
                <Input
                    type="number"
                    id="orderId"
                    placeholder="Enter order ID"
                    value={filters.orderId || ""}
                    onChange={(e) =>
                        handleFilterChange(
                            "orderId",
                            e.target.value ? parseInt(e.target.value) : undefined
                        )
                    }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="userId">User ID</Label>
                <Input
                    type="number"
                    id="userId"
                    placeholder="Enter user ID"
                    value={filters.userId || ""}
                    onChange={(e) =>
                        handleFilterChange(
                            "userId",
                            e.target.value ? parseInt(e.target.value) : undefined
                        )
                    }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="productId">Product ID</Label>
                <Input
                    type="number"
                    id="productId"
                    placeholder="Enter product ID"
                    value={filters.productId || ""}
                    onChange={(e) =>
                        handleFilterChange(
                            "productId",
                            e.target.value ? parseInt(e.target.value) : undefined
                        )
                    }
                />
              </div>
            </div>

            {/* Row 2: Start Date, End Date, Status */}
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
                >
                  <SelectTrigger>
                    <SelectValue placeholder="All Statuses"/>
                  </SelectTrigger>
                  <SelectContent>
                    {ORDER_STATUSES.map((status) => (
                        <SelectItem key={status} value={status}>
                          {status}
                        </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="flex justify-start space-x-2 mt-4">
              <Button onClick={handleSearch} disabled={isLoadingOrders}>
                <Search className="h-4 w-4 mr-2"/>
                Search
              </Button>
              <Button
                  variant="outline"
                  onClick={resetFilters}
                  disabled={isLoadingOrders}
              >
                <RotateCcw className="h-4 w-4 mr-2"/>
                Reset
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Orders Table */}
        <Card>
          <CardHeader>
            <CardTitle>Order Results</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoadingOrders ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <Loader2 className="h-8 w-8 animate-spin text-primary"/>
                  <p className="mt-2 text-muted-foreground">Loading orders...</p>
                </div>
            ) : (
                <div className="rounded-md border">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>ID</TableHead>
                        <TableHead>Code</TableHead>
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
                                  <TableCell>{order.code}</TableCell>
                                  <TableCell>
                                    {order.user
                                        ? `${order.user.firstName} ${order.user.lastName}`
                                        : "N/A"}
                                  </TableCell>
                                  <TableCell>${formatCurrency(order.amount)}</TableCell>
                                  <TableCell>
                                    {getOrderStatusBadge(order.status)}
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
                                            <ChevronUp className="h-4 w-4 mr-2"/>
                                            Hide Details
                                          </>
                                      ) : (
                                          <>
                                            <ChevronDown className="h-4 w-4 mr-2"/>
                                            View Details
                                          </>
                                      )}
                                    </Button>
                                  </TableCell>
                                </TableRow>

                                {/* Order Details */}
                                {selectedOrderId === order.id && (
                                    <TableRow>
                                      <TableCell colSpan={7} className="p-0">
                                        <div className="p-6 bg-muted/30 border-t">
                                          <div className="space-y-6">
                                            <div>
                                              <h6 className="text-lg font-semibold mb-4">
                                                User Information
                                              </h6>
                                              <div
                                                  className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                                <div>
                                        <span className="text-sm font-medium text-muted-foreground">
                                          User ID
                                        </span>
                                                  <p className="text-sm">
                                                    {order.user?.id ?? "N/A"}
                                                  </p>
                                                </div>
                                                <div>
                                        <span className="text-sm font-medium text-muted-foreground">
                                          First Name
                                        </span>
                                                  <p className="text-sm">
                                                    {order.user?.firstName ?? "N/A"}
                                                  </p>
                                                </div>
                                                <div>
                                        <span className="text-sm font-medium text-muted-foreground">
                                          Last Name
                                        </span>
                                                  <p className="text-sm">
                                                    {order.user?.lastName ?? "N/A"}
                                                  </p>
                                                </div>
                                              </div>
                                            </div>

                                            {order.cancellationReason && (
                                                <Alert
                                                    className="border-destructive bg-destructive/10">
                                                  <AlertDescription>
                                                    <strong>Cancellation Reason:</strong>{" "}
                                                    {order.cancellationReason}
                                                  </AlertDescription>
                                                </Alert>
                                            )}

                                            <div>
                                              <h6 className="text-lg font-semibold mb-4">
                                                Order Items
                                              </h6>
                                              <div className="rounded-md border">
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
                                                          <TableCell>
                                                            {item.product.id}
                                                          </TableCell>
                                                          <TableCell>
                                                            {item.product.name}
                                                          </TableCell>
                                                          <TableCell>
                                                            $
                                                            {formatCurrency(
                                                                item.product.price
                                                            )}
                                                          </TableCell>
                                                          <TableCell>
                                                            {item.quantity}
                                                          </TableCell>
                                                          <TableCell>
                                                            $
                                                            {formatCurrency(
                                                                item.product.price *
                                                                item.quantity
                                                            )}
                                                          </TableCell>
                                                        </TableRow>
                                                    ))}
                                                  </TableBody>
                                                </Table>
                                              </div>
                                            </div>
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

        {/* Pagination */}
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

                  {/* Page numbers */}
                  {Array.from(
                      {length: Math.min(5, metadata.totalPages)},
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

export default OrderList;
