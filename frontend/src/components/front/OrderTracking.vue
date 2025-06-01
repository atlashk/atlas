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
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { toast } from 'vue3-toastify';

interface OrderStatuses {
  shortPolling: OrderStatus;
  sse: OrderStatus;
  ws: OrderStatus;
}

interface CanceledReasons {
  shortPolling: string | null;
  sse: string | null;
  ws: string | null;
}

const cartStore = useCartStore();
const currentOrderId = computed(() => cartStore.currentOrderId?.toString() || '');

// Create reactive objects using ref
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

let pollingInterval: ReturnType<typeof setInterval> | null = null;
let stompClient: any = null;
let eventSource: EventSource | null = null;

const NOTIFICATION_BASE_URL = import.meta.env.VITE_NOTIFICATION_BASE_URL;

const updateOrderStatus = (type: keyof OrderStatuses, status: OrderStatus, reason: string | null = null): void => {
  orderStatuses.value[type] = status;
  canceledReasons.value[type] = reason;
};

const startShortPolling = (orderId: string): void => {
  const pollOrder = async (): Promise<void> => {
    try {
      const response = await orderService.getOrderStatus(parseInt(orderId));
      if (response.success && response.data) {
        const { status, canceledReason } = response.data;
        updateOrderStatus('shortPolling', status, canceledReason || null);
        if (status == OrderStatus.CONFIRMED || status == OrderStatus.CANCELED) {
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

  // Initial poll
  pollOrder();

  // Set up interval for polling
  pollingInterval = setInterval(pollOrder, 5000);
};

const stopShortPolling = (): void => {
  if (pollingInterval) {
    clearInterval(pollingInterval);
    pollingInterval = null;
  }
};

const connectSSE = (orderId: string): void => {
  if (eventSource) eventSource.close();

  eventSource = new EventSource(`${NOTIFICATION_BASE_URL}/notification/sse/orders/${orderId}/status`);

  eventSource.addEventListener('open', (event: Event) => {
    console.log('SSE connection established', event);
  });

  // Listen specifically for ORDER_STATUS_CHANGED events
  eventSource.addEventListener('ORDER_STATUS_CHANGED', (event: MessageEvent) => {
    console.log('SSE received ORDER_STATUS_CHANGED event:', event.data);
    try {
      const data = JSON.parse(event.data);
      updateOrderStatus('sse', data.orderStatus, data.canceledReason || null);
    } catch (error: any) {
      console.error('SSE error parsing message: ' + error.message);
    }
  });

  eventSource.addEventListener('error', (error: Event) => {
    console.error('SSE connection error:', error);
    if (eventSource?.readyState === EventSource.CLOSED) {
      console.log('SSE connection closed');
    }
  });
};

const connectWebSocket = (orderId: string): void => {
  if (stompClient) stompClient.disconnect();

  // Create a WebSocket factory function for reconnection support
  const socketFactory = () => {
    return new SockJS(`${NOTIFICATION_BASE_URL}/notification/ws`);
  };

  // Use the factory instead of passing the socket directly
  stompClient = Stomp.over(socketFactory);
  
  // Continue with the rest of your code
  stompClient.debug = (msg: string) => {
    console.log('STOMP Debug:', msg);
  };

  // Set reconnect delay (optional)
  stompClient.reconnectDelay = 30000;

  stompClient.connect({}, () => {
    console.log('WebSocket connection established');
    stompClient.subscribe(`/topic/orders/${orderId}/status`, (message: { body: string }) => {
      console.log('WebSocket received message:', message.body);
      try {
        const { orderStatus, canceledReason } = JSON.parse(message.body);
        updateOrderStatus('ws', orderStatus, canceledReason || null);
      } catch (error: any) {
        console.error('WebSocket error parsing message:', error.message);
      }
    });
  }, (error: Error | string) => {
    console.error('WebSocket connection failed:', error);
  });
};

const cleanup = (): void => {
  stopShortPolling();
  if (stompClient) {
    stompClient.disconnect();
    stompClient = null;
  }
  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }
};

const resetOrderTrackingInfo = (): void => {
  (Object.keys(orderStatuses.value) as Array<keyof OrderStatuses>).forEach((key) => {
    orderStatuses.value[key] = OrderStatus.PROCESSING;
  });
  (Object.keys(canceledReasons.value) as Array<keyof CanceledReasons>).forEach((key) => {
    canceledReasons.value[key] = null;
  });
};

watch(() => currentOrderId.value, (newOrderId) => {
  resetOrderTrackingInfo();
  if (newOrderId) {
    startShortPolling(newOrderId);
    connectSSE(newOrderId);
    connectWebSocket(newOrderId);
  } else {
    cleanup();
  }
}, { immediate: true });

onBeforeUnmount(() => {
  cleanup();
});
</script>
