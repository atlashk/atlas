// Frontend configuration system
export interface AppConfig {
  notification: NotificationConfig;
}

export interface NotificationConfig {
  defaultMethod: 'polling' | 'sse' | 'ws';
  pollingInterval?: number; // in milliseconds
  reconnectAttempts?: number;
  reconnectDelay?: number; // in milliseconds
}

// Default configuration
export const DEFAULT_CONFIG: AppConfig = {
  notification: {
    defaultMethod: 'ws',
    pollingInterval: 5000,
    reconnectAttempts: 3,
    reconnectDelay: 2000,
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

  setNotificationMethod(method: 'polling' | 'sse' | 'ws'): void {
    this.config.notification.defaultMethod = method;
    this.notifySubscribers();
  }
}

export const configStore = new ConfigStore();