<template>
  <div v-if="currentOrderId" class="order-tracking card shadow-sm mt-3">
    <div class="card-body">
      <h6 class="card-title text-center text-primary mb-3">Order Tracking</h6>

      <div class="d-flex justify-content-between">
        <span class="text-muted">Order ID:</span>
        <span class="fw-bold">{{ currentOrderId }}</span>
      </div>

      <!-- Short polling updates -->
      <div class="mt-4">
        <h6 class="text-secondary">Short Polling Updates</h6>
        <div class="d-flex justify-content-between mt-2">
          <span class="text-muted">Status:</span>
          <span :class="getOrderStatusBadgeClasses(orderStatuses.shortPolling)">
            {{ orderStatuses.shortPolling }}
          </span>
        </div>
        <div v-if="canceledReasons.shortPolling" class="mt-2 text-danger">
          <span class="text-muted">Cancellation Reason:</span>
          <p>{{ canceledReasons.shortPolling }}</p>
        </div>
      </div>

      <!-- SSE updates -->
      <div class="mt-4">
        <h6 class="text-secondary">SSE Updates</h6>
        <div class="d-flex justify-content-between mt-2">
          <span class="text-muted">Status:</span>
          <span :class="getOrderStatusBadgeClasses(orderStatuses.sse)">
            {{ orderStatuses.sse }}
          </span>
        </div>
        <div v-if="canceledReasons.sse" class="mt-2 text-danger">
          <span class="text-muted">Cancellation Reason:</span>
          <p>{{ canceledReasons.sse }}</p>
        </div>
      </div>

      <!-- WebSocket updates -->
      <div class="mt-4">
        <h6 class="text-secondary">WebSocket Updates</h6>
        <div class="d-flex justify-content-between mt-2">
          <span class="text-muted">Status:</span>
          <span :class="getOrderStatusBadgeClasses(orderStatuses.ws)">
            {{ orderStatuses.ws }}
          </span>
        </div>
        <div v-if="canceledReasons.ws" class="mt-2 text-danger">
          <span class="text-muted">Cancellation Reason:</span>
          <p>{{ canceledReasons.ws }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { OrderStatus } from '@/interfaces/order.interface';
import { orderService } from '@/services';
import { useCartStore } from '@/stores/cart.store';
import { getOrderStatusBadgeClasses } from '@/utils/formatter.util';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { toast } from 'vue3-toastify';

/**
 * Interface for tracking order statuses across different communication methods
 */
interface OrderStatuses {
  shortPolling: OrderStatus;  // Status from HTTP polling
  sse: OrderStatus;          // Status from Server-Sent Events
  ws: OrderStatus;           // Status from WebSocket
}

/**
 * Interface for tracking cancellation reasons across different communication methods
 */
interface CanceledReasons {
  shortPolling: string | null;
  sse: string | null;
  ws: string | null;
}

// Store and computed properties
const cartStore = useCartStore();
const currentOrderId = computed(() => cartStore.currentOrderId?.toString() || '');

// Reactive state for order statuses and cancellation reasons
const orderStatuses = ref<OrderStatuses>({
  shortPolling: OrderStatus.PROCESSING,
  sse: OrderStatus.PROCESSING,
  ws: OrderStatus.PROCESSING
});

const canceledReasons = ref<CanceledReasons>({
  shortPolling: null,
  sse: null,
  ws: null
});

// Connection management variables
let pollingInterval: ReturnType<typeof setInterval> | null = null;  // HTTP polling timer
let stompClient: Client | null = null;                              // WebSocket STOMP client
let eventSource: EventSource | null = null;                        // SSE connection

// API base URL from environment variables
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

/**
 * Updates the order status and cancellation reason for a specific communication method
 * @param type - The communication method type (shortPolling, sse, ws)
 * @param status - The new order status
 * @param reason - The cancellation reason (if applicable)
 */
const updateOrderStatus = (type: keyof OrderStatuses, status: OrderStatus, reason: string | null = null): void => {
  orderStatuses.value[type] = status;
  canceledReasons.value[type] = reason;
};

/**
 * Starts HTTP polling to check order status every 5 seconds
 * Automatically stops when order reaches a final state (CONFIRMED or CANCELED)
 * @param orderId - The order ID to track
 */
const startShortPolling = (orderId: string): void => {
  const pollOrder = async (): Promise<void> => {
    try {
      const response = await orderService.getOrderStatus(parseInt(orderId));
      if (response.success && response.data) {
        const { status, canceledReason } = response.data;
        updateOrderStatus('shortPolling', status, canceledReason || null);
        
        // Stop polling when order reaches final state to avoid unnecessary requests
        if (status === OrderStatus.CONFIRMED || status === OrderStatus.CANCELED) {
          stopShortPolling();
        }
      } else {
        toast.error(response.errorMessage || 'Failed to fetch order status');
      }
    } catch (error: any) {
      console.error('Error in short polling:', error);
      toast.error('Error fetching order status: ' + (error.message || 'Unknown error'));
    }
  };

  pollOrder(); // Execute initial poll immediately
  pollingInterval = setInterval(pollOrder, 5000); // Poll every 5 seconds
};

/**
 * Stops the HTTP polling interval and cleans up resources
 */
const stopShortPolling = (): void => {
  if (pollingInterval) {
    clearInterval(pollingInterval);
    pollingInterval = null;
  }
};

/**
 * Establishes Server-Sent Events (SSE) connection for real-time order updates
 * SSE provides one-way communication from server to client
 * @param orderId - The order ID to track
 */
const connectSSE = (orderId: string): void => {
  // Close existing connection if any
  if (eventSource) eventSource.close();

  // Create new SSE connection to order-specific endpoint
  eventSource = new EventSource(`${API_BASE_URL}/notification/sse/orders/${orderId}/status`);

  // Handle successful connection
  eventSource.addEventListener('open', () => {
    console.log('SSE connection established');
  });

  // Listen for order status change events
  eventSource.addEventListener('ORDER_STATUS_CHANGED', (event: MessageEvent) => {
    try {
      const data = JSON.parse(event.data);
      updateOrderStatus('sse', data.orderStatus, data.canceledReason || null);
    } catch (error: any) {
      console.error('SSE error parsing message:', error.message);
    }
  });

  // Handle connection errors
  eventSource.addEventListener('error', () => {
    console.error('SSE connection error');
  });
};

/**
 * Establishes WebSocket connection using STOMP protocol for real-time bidirectional communication
 * Uses SockJS for fallback transport methods (WebSocket -> XHR Streaming -> XHR Polling)
 * @param orderId - The order ID to track
 */
const connectWebSocket = (orderId: string): void => {
  // Cleanup existing connection
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }

  // Create new STOMP client with SockJS transport
  stompClient = new Client({
    // SockJS factory for WebSocket connection with fallback transports
    webSocketFactory: () => new SockJS(`${API_BASE_URL}/notification/ws`, null, {
      transports: ['websocket', 'xhr-streaming', 'xhr-polling'], // Fallback order
      timeout: 10000 // 10 second timeout
    }),
    connectHeaders: {},           // Additional headers for STOMP connection
    reconnectDelay: 5000,        // Auto-reconnect after 5 seconds
    heartbeatIncoming: 4000,     // Expect heartbeat from server every 4 seconds
    heartbeatOutgoing: 4000,     // Send heartbeat to server every 4 seconds
    
    // Handle successful STOMP connection
    onConnect: () => {
      if (!stompClient) return;
      
      // Subscribe to order-specific topic for status updates
      stompClient.subscribe(`/topic/orders/${orderId}/status`, (message: { body: string }) => {
        try {
          const { orderStatus, canceledReason } = JSON.parse(message.body);
          updateOrderStatus('ws', orderStatus, canceledReason || null);
        } catch (error: any) {
          console.error('WebSocket error parsing message:', error.message);
        }
      });
    },
    
    // Handle STOMP protocol errors
    onStompError: (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      toast.error('WebSocket connection error');
    },
    
    // Handle WebSocket connection errors
    onWebSocketError: () => {
      console.error('WebSocket connection failed');
      toast.error('WebSocket connection failed');
    },
    
    // Handle WebSocket connection closure
    onWebSocketClose: (event) => {
      // Code 2000 indicates all SockJS transports failed
      if (event.code === 2000) {
        console.error('All WebSocket transports failed');
        toast.error('WebSocket connection failed: All transports failed');
      }
    }
  });

  // Activate the STOMP client to start connection
  stompClient.activate();
};

/**
 * Cleans up all active connections and intervals
 * Called when component unmounts or order ID changes
 */
const cleanup = (): void => {
  // Stop HTTP polling
  stopShortPolling();
  
  // Close WebSocket connection
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
  
  // Close SSE connection
  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }
};

/**
 * Resets all order tracking information to initial state
 * Called when switching to a new order
 */
const resetOrderTrackingInfo = (): void => {
  // Reset all order statuses to PROCESSING
  Object.keys(orderStatuses.value).forEach((key) => {
    orderStatuses.value[key as keyof OrderStatuses] = OrderStatus.PROCESSING;
  });
  
  // Clear all cancellation reasons
  Object.keys(canceledReasons.value).forEach((key) => {
    canceledReasons.value[key as keyof CanceledReasons] = null;
  });
};

/**
 * Watch for order ID changes and manage connections accordingly
 * - When order ID changes: cleanup old connections, reset state, start new connections
 * - When order ID becomes empty: cleanup all connections
 */
watch(() => currentOrderId.value, (newOrderId) => {
  resetOrderTrackingInfo();
  
  if (newOrderId) {
    // Start all three communication methods for the new order
    startShortPolling(newOrderId);
    connectSSE(newOrderId);
    connectWebSocket(newOrderId);
  } else {
    // No active order, cleanup all connections
    cleanup();
  }
}, { immediate: true }); // Execute immediately on component mount

/**
 * Cleanup all connections when component is unmounted
 * Prevents memory leaks and unnecessary network requests
 */
onBeforeUnmount(() => {
  cleanup();
});
</script>
