import { OrderTrackingPayload } from '@/interfaces/payment.interface';
import { configStore } from '@/lib/config';
import { orderApi } from '@/api';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export type NotificationCallback = (payload: OrderTrackingPayload) => void;

export interface NotificationSubscription {
  unsubscribe: () => void;
}

class NotificationService {
  private subscriptions = new Map<string, NotificationSubscription>();
  private pollingIntervals = new Map<string, NodeJS.Timeout>();
  private eventSources = new Map<string, EventSource>();
  private stompClients = new Map<string, Client>();

  /**
   * Subscribe to order notifications using the configured method
   */
  subscribeToOrder(orderId: number, callback: NotificationCallback): NotificationSubscription {
    const subscriptionKey = `order-${orderId}`;
    
    // Unsubscribe existing subscription if any
    this.unsubscribe(subscriptionKey);

    const config = configStore.getNotificationConfig();
    
    switch (config.defaultMethod) {
      case 'polling':
        return this.subscribeWithPolling(orderId, callback, subscriptionKey);
      case 'sse':
        return this.subscribeWithSSE(orderId, callback, subscriptionKey);
      case 'ws':
        return this.subscribeWithWebSocket(orderId, callback, subscriptionKey);
      default:
        throw new Error(`Unsupported notification method: ${config.defaultMethod}`);
    }
  }

  private subscribeWithPolling(
    orderId: number, 
    callback: NotificationCallback, 
    subscriptionKey: string
  ): NotificationSubscription {
    const config = configStore.getNotificationConfig();
    let lastStatus: string | null = null;

    const poll = async () => {
      try {
        const response = await orderApi.getOrderStatus(orderId);
        
        // Only trigger callback if status changed
        if (response.data.status !== lastStatus) {
          lastStatus = response.data.status;
          callback({
            orderId,
            orderStatus: response.data.status,
            cancellationReason: response.data.cancellationReason,
          });
        }
      } catch (error) {
        console.error('Polling error:', error);
      }
    };

    // Initial poll
    poll();

    // Set up interval
    const interval = setInterval(poll, config.pollingInterval || 5000);
    this.pollingIntervals.set(subscriptionKey, interval);

    const subscription: NotificationSubscription = {
      unsubscribe: () => {
        const interval = this.pollingIntervals.get(subscriptionKey);
        if (interval) {
          clearInterval(interval);
          this.pollingIntervals.delete(subscriptionKey);
        }
        this.subscriptions.delete(subscriptionKey);
      }
    };

    this.subscriptions.set(subscriptionKey, subscription);
    return subscription;
  }

  private subscribeWithSSE(
    orderId: number, 
    callback: NotificationCallback, 
    subscriptionKey: string
  ): NotificationSubscription {
    const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;
    const eventSource = new EventSource(`${API_BASE_URL}/notification-svc/sse/orders/${orderId}/status`);

    eventSource.onmessage = (event) => {
      try {
        const payload: OrderTrackingPayload = JSON.parse(event.data);
        callback(payload);
      } catch (error) {
        console.error('SSE message parsing error:', error);
      }
    };

    eventSource.onerror = (error) => {
      console.error('SSE connection error:', error);
    };

    this.eventSources.set(subscriptionKey, eventSource);

    const subscription: NotificationSubscription = {
      unsubscribe: () => {
        const eventSource = this.eventSources.get(subscriptionKey);
        if (eventSource) {
          eventSource.close();
          this.eventSources.delete(subscriptionKey);
        }
        this.subscriptions.delete(subscriptionKey);
      }
    };

    this.subscriptions.set(subscriptionKey, subscription);
    return subscription;
  }

  private subscribeWithWebSocket(
    orderId: number, 
    callback: NotificationCallback, 
    subscriptionKey: string
  ): NotificationSubscription {
    const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;
    const config = configStore.getNotificationConfig();

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/notification-svc/ws`),
      reconnectDelay: config.reconnectDelay || 2000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        stompClient.subscribe(
          `/topic/orders/${orderId}/status`,
          (message: { body: string }) => {
            try {
              const payload: OrderTrackingPayload = JSON.parse(message.body);
              callback(payload);
            } catch (error) {
              console.error('WebSocket message parsing error:', error);
            }
          }
        );
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message']);
      },
      onWebSocketError: () => {
        console.error('WebSocket connection failed');
      },
      onWebSocketClose: () => {
        console.log('WebSocket connection closed');
      },
    });

    stompClient.activate();
    this.stompClients.set(subscriptionKey, stompClient);

    const subscription: NotificationSubscription = {
      unsubscribe: () => {
        const client = this.stompClients.get(subscriptionKey);
        if (client) {
          client.deactivate();
          this.stompClients.delete(subscriptionKey);
        }
        this.subscriptions.delete(subscriptionKey);
      }
    };

    this.subscriptions.set(subscriptionKey, subscription);
    return subscription;
  }

  private unsubscribe(subscriptionKey: string): void {
    const subscription = this.subscriptions.get(subscriptionKey);
    if (subscription) {
      subscription.unsubscribe();
    }
  }

  /**
   * Unsubscribe from all notifications
   */
  unsubscribeAll(): void {
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();
  }

  /**
   * Change notification method for all active subscriptions
   */
  changeNotificationMethod(method: 'polling' | 'sse' | 'ws'): void {
    configStore.setNotificationMethod(method);
    
    // Re-subscribe all active subscriptions with new method
    const activeSubscriptions = Array.from(this.subscriptions.keys());
    // Note: This would require storing callback references, 
    // which is complex. For now, consumers should re-subscribe manually.
    console.log('Notification method changed to:', method);
    console.log('Active subscriptions that need re-subscription:', activeSubscriptions);
  }
}

export const notificationService = new NotificationService();