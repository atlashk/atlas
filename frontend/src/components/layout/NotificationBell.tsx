"use client";

import { notificationApi } from "@/api/notification.api";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useRealtime } from "@/contexts/RealtimeContext";
import { InAppNotification } from "@/interfaces/notification.interface";
import { Bell } from "lucide-react";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Button } from "../ui/button";
import DOMPurify from "isomorphic-dompurify";

export default function NotificationBell() {
  const [notifications, setNotifications] = useState<InAppNotification[]>([]);
  const [hasUnread, setHasUnread] = useState(false);
  const { lastMessage } = useRealtime();

  useEffect(() => {
    if (lastMessage) {
      toast.info(
        <div
          dangerouslySetInnerHTML={{
            __html: DOMPurify.sanitize(lastMessage.message),
          }}
        />
      );
      // Immediately show unread badge when a realtime message arrives
      setHasUnread(true);
      fetchNotifications();
    }
  }, [lastMessage]);

  const fetchNotifications = async () => {
    try {
      const response = await notificationApi.listInAppNotifications();
      setNotifications(response.data);
      setHasUnread(response.data.some((n: InAppNotification) => !n.read));
    } catch (error) {
      console.error("Failed to fetch notifications:", error);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      // Optimistically clear unread badge
      setHasUnread(false);
      fetchNotifications();
    } catch (error) {
      console.error("Failed to mark all as read:", error);
    }
  };

  const handleOpenChange = (open: boolean) => {
    if (open) {
      fetchNotifications();
    }
  };

  return (
    <DropdownMenu onOpenChange={handleOpenChange}>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          className="relative text-white hover:text-gray-300 hover:bg-white/10 p-3"
        >
          <Bell style={{ width: "1.25rem", height: "1.25rem" }} />
          {hasUnread && (
            <span className="absolute top-0 right-0 block h-2 w-2 rounded-full bg-red-500" />
          )}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-80">
        <div className="max-h-96 overflow-y-auto">
          {notifications.length === 0 ? (
            <p className="p-4 text-sm text-gray-500">No notifications</p>
          ) : (
            notifications.map((notification) => (
              <DropdownMenuItem
                key={notification.id}
                className={`flex items-start p-4 transition-colors hover:bg-gray-100 dark:hover:bg-gray-800 ${
                  !notification.read ? "bg-blue-50 dark:bg-blue-900/30" : ""
                }`}
              >
                <div className="flex-1">
                  <div
                    className="text-sm"
                    // Render sanitized HTML message content
                    dangerouslySetInnerHTML={{
                      __html: DOMPurify.sanitize(notification.message),
                    }}
                  />
                  <p className="mt-2 text-xs text-gray-500">
                    {new Date(notification.deliveredAt).toLocaleString()}
                  </p>
                </div>
              </DropdownMenuItem>
            ))
          )}
        </div>
        {notifications.length > 0 && (
          <div className="p-2 text-center">
            <Button variant="link" onClick={handleMarkAllAsRead}>
              Mark all as read
            </Button>
          </div>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
