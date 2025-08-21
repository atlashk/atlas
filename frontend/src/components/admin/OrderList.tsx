"use client";

import React, { useState, useEffect, useCallback } from "react";
import { orderService } from "@/services";
import {
  formatCurrency,
  formatDate,
  getOrderStatusBadge,
} from "@/utils/formatter.util";
import { toast } from "sonner";
import {
  OrderStatus,
  type ListOrderFilters,
  type Order,
} from "@/interfaces/order.interface";

const OrderList: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoadingOrders, setIsLoadingOrders] = useState(true);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [filters, setFilters] = useState<ListOrderFilters>({
    orderId: undefined,
    userId: undefined,
    status: "" as const,
    startDate: undefined,
    endDate: undefined,
    page: 1,
    size: 20,
  });
  const [metadata, setMetadata] = useState({
    currentPage: 1,
    pageSize: 20,
    totalPages: 1,
    totalRecords: 0,
  });

  // Order statuses for dropdown
  const orderStatuses = Object.values(OrderStatus);

  const toggleDetails = (orderId: number) => {
    setSelectedOrderId(selectedOrderId === orderId ? null : orderId);
  };

  const applyFilters = useCallback(
    async (page: number) => {
      setIsLoadingOrders(true);
      try {
        const updatedFilters = { ...filters, page };
        setFilters(updatedFilters);
        setMetadata((prev) => ({ ...prev, currentPage: page }));

        const response = await orderService.listOrder(updatedFilters);

        setOrders(response.data);
        if (response.metadata) {
          setMetadata(response.metadata);
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

  const changePage = (newPage: number) => {
    if (newPage >= 1 && newPage <= metadata.totalPages) {
      applyFilters(newPage);
    }
  };

  const resetFilters = () => {
    const resetFilters: ListOrderFilters = {
      orderId: undefined,
      userId: undefined,
      status: "" as const,
      startDate: undefined,
      endDate: undefined,
      page: 1,
      size: 20,
    };
    setFilters(resetFilters);
    applyFilters(1);
  };

  const handleFilterChange = (
    field: keyof ListOrderFilters,
    value: string | number | boolean | OrderStatus | undefined
  ) => {
    setFilters((prev) => ({ ...prev, [field]: value }));
  };

  const handleSearch = () => {
    applyFilters(1);
  };

  // Initial load
  useEffect(() => {
    applyFilters(1);
  }, [applyFilters]);

  return (
    <div className="container-fluid py-4">
      <div className="row">
        <div className="col-12">
          <div className="card">
            <div className="card-header">
              <h5 className="card-title mb-0">
                <i className="bi bi-cart-check me-2"></i>
                Order Management
              </h5>
            </div>

            {/* Filters */}
            <div className="card-body border-bottom">
              <div className="row g-3">
                <div className="col-md-2">
                  <label htmlFor="orderId" className="form-label">
                    Order ID
                  </label>
                  <input
                    type="number"
                    className="form-control"
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
                <div className="col-md-2">
                  <label htmlFor="userId" className="form-label">
                    User ID
                  </label>
                  <input
                    type="number"
                    className="form-control"
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
                <div className="col-md-2">
                  <label htmlFor="status" className="form-label">
                    Status
                  </label>
                  <select
                    className="form-select"
                    id="status"
                    value={filters.status}
                    onChange={(e) =>
                      handleFilterChange("status", e.target.value)
                    }
                  >
                    <option value="">All Statuses</option>
                    {orderStatuses.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-md-2">
                  <label htmlFor="startDate" className="form-label">
                    Start Date
                  </label>
                  <input
                    type="date"
                    className="form-control"
                    id="startDate"
                    value={filters.startDate || ""}
                    onChange={(e) =>
                      handleFilterChange(
                        "startDate",
                        e.target.value || undefined
                      )
                    }
                  />
                </div>
                <div className="col-md-2">
                  <label htmlFor="endDate" className="form-label">
                    End Date
                  </label>
                  <input
                    type="date"
                    className="form-control"
                    id="endDate"
                    value={filters.endDate || ""}
                    onChange={(e) =>
                      handleFilterChange("endDate", e.target.value || undefined)
                    }
                  />
                </div>
                <div className="col-md-2 d-flex align-items-end">
                  <div className="btn-group w-100">
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={handleSearch}
                      disabled={isLoadingOrders}
                    >
                      <i className="bi bi-search me-1"></i>
                      Search
                    </button>
                    <button
                      type="button"
                      className="btn btn-outline-secondary"
                      onClick={resetFilters}
                      disabled={isLoadingOrders}
                    >
                      <i className="bi bi-arrow-clockwise me-1"></i>
                      Reset
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Orders Table */}
            <div className="table-responsive">
              {isLoadingOrders ? (
                <div className="text-center py-5">
                  <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                  <p className="mt-2 text-muted">Loading orders...</p>
                </div>
              ) : (
                <table className="table table-hover mb-0">
                  <thead className="table-light">
                    <tr>
                      <th scope="col" className="px-4">
                        ID
                      </th>
                      <th scope="col" className="px-4">
                        Code
                      </th>
                      <th scope="col" className="px-4">
                        User
                      </th>
                      <th scope="col" className="px-4">
                        Amount
                      </th>
                      <th scope="col" className="px-4">
                        Status
                      </th>
                      <th scope="col" className="px-4">
                        Created At
                      </th>
                      <th scope="col" className="px-4">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="text-center py-4 text-muted">
                          No orders found
                        </td>
                      </tr>
                    ) : (
                      orders.map((order) => (
                        <React.Fragment key={order.id}>
                          <tr>
                            <td className="px-4">{order.id}</td>
                            <td className="px-4">{order.code}</td>
                            <td className="px-4">
                              {order.user
                                ? `${order.user.firstName} ${order.user.lastName}`
                                : "N/A"}
                            </td>
                            <td className="px-4">
                              ${formatCurrency(order.amount)}
                            </td>
                            <td className="px-4">
                              {getOrderStatusBadge(order.status)}
                            </td>
                            <td className="px-4">
                              {formatDate(order.createdAt)}
                            </td>
                            <td className="px-4">
                              <button
                                className="btn btn-sm btn-outline-secondary"
                                onClick={() => toggleDetails(order.id)}
                              >
                                {selectedOrderId === order.id
                                  ? "Hide Details"
                                  : "View Details"}
                              </button>
                            </td>
                          </tr>

                          {/* Order Details */}
                          {selectedOrderId === order.id && (
                            <tr>
                              <td colSpan={7} className="p-0">
                                <div className="p-4 border-top">
                                  <h6 className="mb-3">User Information</h6>
                                  <dl className="row mb-3">
                                    <dt className="col-sm-3">User ID</dt>
                                    <dd className="col-sm-9">
                                      {order.user?.id ?? "N/A"}
                                    </dd>
                                    <dt className="col-sm-3">First Name</dt>
                                    <dd className="col-sm-9">
                                      {order.user?.firstName ?? "N/A"}
                                    </dd>
                                    <dt className="col-sm-3">Last Name</dt>
                                    <dd className="col-sm-9">
                                      {order.user?.lastName ?? "N/A"}
                                    </dd>
                                  </dl>

                                  {order.cancelReason && (
                                    <div className="alert alert-danger mb-3">
                                      <strong>Cancellation Reason:</strong>{" "}
                                      {order.cancelReason}
                                    </div>
                                  )}

                                  <h6 className="mb-3">Order Items</h6>
                                  <div className="table-responsive">
                                    <table className="table table-bordered">
                                      <thead className="table-light">
                                        <tr>
                                          <th scope="col">Product ID</th>
                                          <th scope="col">Product Name</th>
                                          <th scope="col">Price</th>
                                          <th scope="col">Quantity</th>
                                          <th scope="col">Subtotal</th>
                                        </tr>
                                      </thead>
                                      <tbody>
                                        {order.orderItems?.map((item) => (
                                          <tr key={item.product.id}>
                                            <td>{item.product.id}</td>
                                            <td>{item.product.name}</td>
                                            <td>
                                              $
                                              {formatCurrency(
                                                item.product.price
                                              )}
                                            </td>
                                            <td>{item.quantity}</td>
                                            <td>
                                              $
                                              {formatCurrency(
                                                item.product.price *
                                                  item.quantity
                                              )}
                                            </td>
                                          </tr>
                                        ))}
                                      </tbody>
                                    </table>
                                  </div>
                                </div>
                              </td>
                            </tr>
                          )}
                        </React.Fragment>
                      ))
                    )}
                  </tbody>
                </table>
              )}
            </div>

            {/* Pagination */}
            <div className="card-footer bg-light py-3">
              <div className="d-flex justify-content-between align-items-center">
                <span className="text-muted">
                  Page {metadata.currentPage} of {metadata.totalPages}
                  <span className="ms-2">
                    ({metadata.totalRecords} records)
                  </span>
                </span>
                <div className="btn-group">
                  <button
                    onClick={() => changePage(metadata.currentPage - 1)}
                    disabled={metadata.currentPage <= 1}
                    className="btn btn-outline-secondary px-3"
                  >
                    Previous
                  </button>
                  <button
                    onClick={() => changePage(metadata.currentPage + 1)}
                    disabled={metadata.currentPage >= metadata.totalPages}
                    className="btn btn-outline-secondary px-3"
                  >
                    Next
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrderList;
