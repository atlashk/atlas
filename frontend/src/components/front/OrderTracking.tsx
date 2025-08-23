import { orderApi } from "@/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useCartStore } from "@/stores";
import { getOrderStatusBadge } from "@/utils/formatter.util";
import { Client } from "@stomp/stompjs";
import React, { useCallback, useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { toast } from "sonner";

/**
 * Interface for tracking order statuses across different communication methods
 */
interface OrderStatuses {
  shortPolling: string; // Status from HTTP polling
  sse: string; // Status from Server-Sent Events
  ws: string; // Status from WebSocket
}

/**
 * Interface for tracking cancellation reasons across different communication methods
 */
interface CanceledReasons {
  shortPolling: string | null;
  sse: string | null;
  ws: string | null;
}

const DEFAULT_ORDER_STATUS = "PROCESSING";

const OrderTracking: React.FC = () => {
  const { currentOrderId } = useCartStore();

  // Reactive state for order statuses and cancellation reasons
  const [orderStatuses, setOrderStatuses] = useState<OrderStatuses>({
    shortPolling: DEFAULT_ORDER_STATUS,
    sse: DEFAULT_ORDER_STATUS,
    ws: DEFAULT_ORDER_STATUS,
  });

  const [canceledReasons, setCanceledReasons] = useState<CanceledReasons>({
    shortPolling: null,
    sse: null,
    ws: null,
  });

  // Connection management refs
  const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const stompClientRef = useRef<Client | null>(null);
  const eventSourceRef = useRef<EventSource | null>(null);

  // API base URL from environment variables
  const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

  /**
   * Updates the order status and cancellation reason for a specific communication method
   * @param type - The communication method type (shortPolling, sse, ws)
   * @param status - The new order status
   * @param reason - The cancellation reason (if applicable)
   */
  const updateOrderStatus = useCallback(
    (
      type: keyof OrderStatuses,
      status: string,
      reason: string | null = null
    ): void => {
      setOrderStatuses((prev) => ({ ...prev, [type]: status }));
      setCanceledReasons((prev) => ({ ...prev, [type]: reason }));
    },
    []
  );

  /**
   * Stops the HTTP polling interval and cleans up resources
   */
  const stopShortPolling = useCallback((): void => {
    if (pollingIntervalRef.current) {
      clearInterval(pollingIntervalRef.current);
      pollingIntervalRef.current = null;
    }
  }, []);

  /**
   * Starts HTTP polling to check order status every 5 seconds
   * Automatically stops when order reaches a final state (CONFIRMED or CANCELED)
   * @param orderId - The order ID to track
   */
  const startShortPolling = useCallback(
    (orderId: number): void => {
      const pollOrder = async (): Promise<void> => {
        try {
          const response = await orderApi.getOrderStatus(orderId);
          if (response.success && response.data) {
            const { status, canceledReason } = response.data;
            updateOrderStatus("shortPolling", status, canceledReason || null);

            // Stop polling when order reaches final state to avoid unnecessary requests
            if (status === "CONFIRMED" || status === "CANCELED") {
              stopShortPolling();
            }
          } else {
            toast.error(
              response.errorMessage || "Failed to fetch order status"
            );
          }
        } catch (error: unknown) {
          console.error("Error in short polling:", error);
          const errorMessage =
            error instanceof Error ? error.message : "Unknown error";
          toast.error("Error fetching order status: " + errorMessage);
        }
      };

      pollOrder(); // Execute initial poll immediately
      pollingIntervalRef.current = setInterval(pollOrder, 5000); // Poll every 5 seconds
    },
    [updateOrderStatus, stopShortPolling]
  );

  /**
   * Establishes Server-Sent Events (SSE) connection for real-time order updates
   * SSE provides one-way communication from server to client
   * @param orderId - The order ID to track
   */
  const connectSSE = useCallback(
    (orderId: number): void => {
      // Close existing connection if any
      if (eventSourceRef.current) eventSourceRef.current.close();

      // Create new SSE connection to order-specific endpoint
      eventSourceRef.current = new EventSource(
        `${API_BASE_URL}/notification-svc/sse/orders/${orderId}/status`
      );

      // Handle successful connection
      eventSourceRef.current.addEventListener("open", () => {
        console.log("SSE connection established");
      });

      // Listen for order status change events
      eventSourceRef.current.addEventListener(
        "ORDER_STATUS_CHANGED",
        (event: MessageEvent) => {
          try {
            const data = JSON.parse(event.data);
            updateOrderStatus(
              "sse",
              data.orderStatus,
              data.canceledReason || null
            );
          } catch (error: unknown) {
            const errorMessage =
              error instanceof Error ? error.message : "Unknown error";
            console.error("SSE error parsing message:", errorMessage);
          }
        }
      );

      // Handle connection errors
      eventSourceRef.current.addEventListener("error", () => {
        console.error("SSE connection error");
      });
    },
    [API_BASE_URL, updateOrderStatus]
  );

  /**
   * Establishes WebSocket connection using STOMP protocol for real-time bidirectional communication
   * Uses SockJS for fallback transport methods (WebSocket -> XHR Streaming -> XHR Polling)
   * @param orderId - The order ID to track
   */
  const connectWebSocket = useCallback(
    (orderId: number): void => {
      // Cleanup existing connection
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        stompClientRef.current = null;
      }

      // Create new STOMP client with SockJS transport
      stompClientRef.current = new Client({
        // SockJS factory for WebSocket connection with fallback transports
        webSocketFactory: () =>
          new SockJS(`${API_BASE_URL}/notification-svc/ws`, null, {
            transports: ["websocket", "xhr-streaming", "xhr-polling"], // Fallback order
            timeout: 10000, // 10 second timeout
          }),
        connectHeaders: {}, // Additional headers for STOMP connection
        reconnectDelay: 5000, // Auto-reconnect after 5 seconds
        heartbeatIncoming: 4000, // Expect heartbeat from server every 4 seconds
        heartbeatOutgoing: 4000, // Send heartbeat to server every 4 seconds

        // Handle successful STOMP connection
        onConnect: () => {
          if (!stompClientRef.current) return;

          // Subscribe to order-specific topic for status updates
          stompClientRef.current.subscribe(
            `/topic/orders/${orderId}/status`,
            (message: { body: string }) => {
              try {
                const { orderStatus, canceledReason } = JSON.parse(
                  message.body
                );
                updateOrderStatus("ws", orderStatus, canceledReason || null);
              } catch (error: unknown) {
                const errorMessage =
                  error instanceof Error ? error.message : "Unknown error";
                console.error("WebSocket error parsing message:", errorMessage);
              }
            }
          );
        },

        // Handle STOMP protocol errors
        onStompError: (frame) => {
          console.error("STOMP error:", frame.headers["message"]);
          toast.error("WebSocket connection error");
        },

        // Handle WebSocket connection errors
        onWebSocketError: () => {
          console.error("WebSocket connection failed");
          toast.error("WebSocket connection failed");
        },

        // Handle WebSocket connection closure
        onWebSocketClose: (event) => {
          // Code 2000 indicates all SockJS transports failed
          if (event.code === 2000) {
            console.error("All WebSocket transports failed");
            toast.error("WebSocket connection failed: All transports failed");
          }
        },
      });

      // Activate the STOMP client to start connection
      stompClientRef.current.activate();
    },
    [API_BASE_URL, updateOrderStatus]
  );

  /**
   * Cleans up all active connections and intervals
   * Called when component unmounts or order ID changes
   */
  const cleanup = useCallback((): void => {
    // Stop HTTP polling
    stopShortPolling();

    // Close WebSocket connection
    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
      stompClientRef.current = null;
    }

    // Close SSE connection
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
  }, [stopShortPolling]);

  /**
   * Resets all order tracking information to initial state
   * Called when switching to a new order
   */
  const resetOrderTrackingInfo = useCallback((): void => {
    // Reset all order statuses to DEFAULT_ORDER_STATUS
    setOrderStatuses({
      shortPolling: DEFAULT_ORDER_STATUS,
      sse: DEFAULT_ORDER_STATUS,
      ws: DEFAULT_ORDER_STATUS,
    });

    // Reset all cancellation reasons
    setCanceledReasons({
      shortPolling: null,
      sse: null,
      ws: null,
    });
  }, []);

  /**
   * Effect to handle order ID changes and manage connections accordingly
   * - When order ID changes: cleanup old connections, reset state, start new connections
   * - When order ID becomes empty: cleanup all connections
   */
  useEffect(() => {
    resetOrderTrackingInfo();

    if (currentOrderId) {
      // Start all three communication methods for the new order
      startShortPolling(currentOrderId);
      connectSSE(currentOrderId);
      connectWebSocket(currentOrderId);
    } else {
      // No active order, cleanup all connections
      cleanup();
    }

    // Cleanup function for when dependencies change or component unmounts
    return cleanup;
  }, [
    currentOrderId,
    startShortPolling,
    connectSSE,
    connectWebSocket,
    cleanup,
    resetOrderTrackingInfo,
  ]);

  // Don't render if no current order or if order tracking should not be shown
  if (!currentOrderId) {
    return null;
  }

  return (
    <Card className="order-tracking shadow-sm mt-6">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-blue-600">Order Tracking</CardTitle>
        </div>
      </CardHeader>
      <CardContent>
        <div className="flex justify-between items-center mb-6">
          <span className="text-gray-500">Order ID:</span>
          <span className="font-bold">{currentOrderId}</span>
        </div>

        {/* Short polling updates */}
        <div className="space-y-4">
          <div className="border-b border-gray-200 pb-4">
            <div className="flex items-center gap-2 mb-3">
              <h6 className="text-gray-600 font-medium">
                Short Polling Updates
              </h6>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-500">Status:</span>
              {getOrderStatusBadge(orderStatuses.shortPolling)}
            </div>
            {canceledReasons.shortPolling && (
              <div className="mt-3">
                <span className="text-gray-500">Cancellation Reason:</span>
                <p className="text-red-600 mt-1">
                  {canceledReasons.shortPolling}
                </p>
              </div>
            )}
          </div>

          {/* SSE updates */}
          <div className="border-b border-gray-200 pb-4">
            <div className="flex items-center gap-2 mb-3">
              <h6 className="text-gray-600 font-medium">SSE Updates</h6>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-500">Status:</span>
              {getOrderStatusBadge(orderStatuses.sse)}
            </div>
            {canceledReasons.sse && (
              <div className="mt-3">
                <span className="text-gray-500">Cancellation Reason:</span>
                <p className="text-red-600 mt-1">{canceledReasons.sse}</p>
              </div>
            )}
          </div>

          {/* WebSocket updates */}
          <div>
            <div className="flex items-center gap-2 mb-3">
              <h6 className="text-gray-600 font-medium">WebSocket Updates</h6>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-500">Status:</span>
              {getOrderStatusBadge(orderStatuses.ws)}
            </div>
            {canceledReasons.ws && (
              <div className="mt-3">
                <span className="text-gray-500">Cancellation Reason:</span>
                <p className="text-red-600 mt-1">{canceledReasons.ws}</p>
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default OrderTracking;
