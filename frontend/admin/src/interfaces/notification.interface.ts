export interface InAppNotification {
  id: string;
  message: string;
  deliveredAt: string;
  read: boolean;
}

export interface InAppServiceInfo {
  serviceType: string;
}
