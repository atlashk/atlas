// Frontend configuration system
export interface AppConfig {
  notification: NotificationConfig;
  payment: PaymentConfig;
}

export interface NotificationConfig {
  defaultMethod: 'polling' | 'sse' | 'ws';
  pollingInterval?: number; // in milliseconds
  reconnectAttempts?: number;
  reconnectDelay?: number; // in milliseconds
}

export interface PaymentConfig {
  defaultGateway: 'stripe';
  gateways: {
    stripe: StripeConfig;
  };
}

export interface StripeConfig {
  publishableKey: string;
  enabled: boolean;
}

// Default configuration
export const DEFAULT_CONFIG: AppConfig = {
  notification: {
    defaultMethod: 'ws',
    pollingInterval: 5000,
    reconnectAttempts: 3,
    reconnectDelay: 2000,
  },
  payment: {
    defaultGateway: 'stripe',
    gateways: {
      stripe: {
        publishableKey: process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY || '',
        enabled: true,
      },
    },
  },
};

// Configuration store
class ConfigStore {
  private config: AppConfig = DEFAULT_CONFIG;
  private subscribers: ((config: AppConfig) => void)[] = [];

  getConfig(): AppConfig {
    return this.config;
  }

  updateConfig(newConfig: Partial<AppConfig>): void {
    this.config = {
      ...this.config,
      ...newConfig,
      notification: {
        ...this.config.notification,
        ...newConfig.notification,
      },
      payment: {
        ...this.config.payment,
        ...newConfig.payment,
        gateways: {
          ...this.config.payment.gateways,
          ...newConfig.payment?.gateways,
        },
      },
    };
    this.notifySubscribers();
  }

  subscribe(callback: (config: AppConfig) => void): () => void {
    this.subscribers.push(callback);
    return () => {
      const index = this.subscribers.indexOf(callback);
      if (index > -1) {
        this.subscribers.splice(index, 1);
      }
    };
  }

  private notifySubscribers(): void {
    this.subscribers.forEach(callback => callback(this.config));
  }

  getNotificationConfig(): NotificationConfig {
    return this.config.notification;
  }

  getPaymentConfig(): PaymentConfig {
    return this.config.payment;
  }

  setNotificationMethod(method: 'polling' | 'sse' | 'ws'): void {
    this.config.notification.defaultMethod = method;
    this.notifySubscribers();
  }

  setDefaultPaymentGateway(gateway: 'stripe'): void {
    this.config.payment.defaultGateway = gateway;
    this.notifySubscribers();
  }

  setPaymentGatewayEnabled(gateway: 'stripe', enabled: boolean): void {
    this.config.payment.gateways[gateway].enabled = enabled;
    this.notifySubscribers();
  }

  updatePaymentGatewayConfig(gateway: 'stripe', config: Partial<StripeConfig>): void {
    this.config.payment.gateways[gateway] = {
      ...this.config.payment.gateways[gateway],
      ...config,
    };
    this.notifySubscribers();
  }
}

export const configStore = new ConfigStore();