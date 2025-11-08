"use client";

import { serviceInfoCache } from "@/services/serviceInfoCache";
import { useUserStore } from "@/stores/user.store";
import { Client as StompClient } from "@stomp/stompjs";
import React, {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import SockJS from "sockjs-client";

// Define a more specific type for notification messages to avoid 'any'
interface NotificationMessage {
  id: string;
  message: string;
  read: boolean;
  deliveredAt: string;
}

// Backend event payloads
type InAppPayload = {
  message: string;
  deliveredAt: string;
};

type WebSocketEventMsg = {
  eventId: string;
  payload: InAppPayload;
};

// Normalize various backend realtime payloads into NotificationMessage
function normalizeToNotificationMessage(input: unknown, fallbackId?: string): NotificationMessage {
  const possibleEvent = input as Partial<WebSocketEventMsg> | undefined;
  const payload: InAppPayload = (possibleEvent && possibleEvent.payload)
    ? possibleEvent.payload
    : (input as InAppPayload);

  return {
    id: (possibleEvent && typeof possibleEvent.eventId === "string" ? possibleEvent.eventId : (fallbackId ?? "")),
    message: payload?.message ?? "",
    read: false,
    deliveredAt: payload?.deliveredAt ?? new Date().toISOString(),
  };
}

interface RealtimeContextProps {
  connectionStatus: string;
  lastMessage: NotificationMessage | null;
}

const RealtimeContext = createContext<RealtimeContextProps | undefined>(
  undefined
);

export const RealtimeProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [connectionStatus, setConnectionStatus] =
    useState<string>("disconnected");
  const [lastMessage, setLastMessage] = useState<NotificationMessage | null>(
    null
  );
  const connectionRef = useRef<EventSource | StompClient | null>(null);
  const isConnectingRef = useRef<boolean>(false);

  // Get user profile from the Zustand store
  const { profile } = useUserStore();
  const userId = profile?.id;

  useEffect(() => {
    // Only connect if a user ID is available from the store
    if (!userId) {
      // Ensure any existing connections are closed if the user logs out or profile is not available
      if (connectionRef.current) {
        const currentConn: any = connectionRef.current;
        if (typeof currentConn.deactivate === "function") {
          currentConn.deactivate();
        } else if (typeof currentConn.close === "function") {
          currentConn.close();
        }
        connectionRef.current = null;
        setConnectionStatus("disconnected");
      }
      return;
    }

    // Avoid duplicate connections
    if (isConnectingRef.current || connectionRef.current) {
      return;
    }

    const fetchServiceInfoAndConnect = async () => {
      isConnectingRef.current = true;
      try {
        // Use cache instead of calling the API directly
        const serviceInfo = await serviceInfoCache.getServiceInfo();

        // Close existing connection before creating a new one
        if (connectionRef.current) {
          const currentConn: any = connectionRef.current;
          if (typeof currentConn.deactivate === "function") {
            currentConn.deactivate();
          } else if (typeof currentConn.close === "function") {
            currentConn.close();
          }
          connectionRef.current = null;
        }

        if (serviceInfo.serviceType === "sse") {
          console.log("Connecting to SSE");
          const eventSource = new EventSource(
            `${process.env.NEXT_PUBLIC_API_BASE_URL}/services/notification/sse/inapp/${userId}`,
            { withCredentials: true }
          );

          // Optimistically set the connection status.
          // The `onerror` handler will catch any connection failures.
          setConnectionStatus("connected");
          console.log("SSE connection initiated");

          // Handle default SSE messages (without named event)
          eventSource.onmessage = (event) => {
            console.log("SSE onmessage raw:", event.data);
            try {
              const raw = JSON.parse(event.data);
              const normalized = normalizeToNotificationMessage(raw, (event as any).lastEventId);
              console.log("SSE onmessage normalized:", normalized);
              setLastMessage(normalized);
            } catch (e) {
              console.warn("Failed to parse SSE message:", e);
            }
          };

          // Handle named SSE event: inapp-notification
          eventSource.addEventListener(
            "inapp-notification",
            (event: MessageEvent) => {
              console.log("SSE inapp-notification raw:", event.data);
              try {
                const raw = JSON.parse(event.data as string);
                const normalized = normalizeToNotificationMessage(raw, (event as any).lastEventId);
                console.log("SSE inapp-notification normalized:", normalized);
                setLastMessage(normalized);
              } catch (e) {
                console.warn(
                  "Failed to parse inapp-notification SSE event:",
                  e
                );
              }
            }
          );

          eventSource.onerror = (error) => {
            console.error("SSE error:", error);
            setConnectionStatus("disconnected");
            eventSource.close();
          };

          connectionRef.current = eventSource;
        } else if (serviceInfo.serviceType === "websocket") {
          console.log("Connecting to WebSocket (STOMP)");
          const endpoint = `${process.env.NEXT_PUBLIC_API_BASE_URL}/services/notification/ws`;
          const client = new StompClient({
            webSocketFactory: () => new SockJS(endpoint),
            reconnectDelay: 5000,
            onConnect: () => {
              setConnectionStatus("connected");
              console.log("STOMP connection established");
              client.subscribe(`/topic/inapp/${userId}`, (message) => {
                try {
                  const raw = JSON.parse(message.body);
                  console.log("STOMP inapp-notification raw:", raw);
                  const normalized = normalizeToNotificationMessage(raw);
                  setLastMessage(normalized);
                } catch (e) {
                  console.warn("Failed to parse STOMP message:", e);
                }
              });
            },
            onWebSocketClose: () => {
              setConnectionStatus("disconnected");
              console.log("WebSocket connection closed");
            },
            onWebSocketError: (error) => {
              console.error("WebSocket error:", error);
              setConnectionStatus("disconnected");
            },
            onStompError: (frame) => {
              console.error("STOMP error:", frame);
            },
          });

          client.activate();
          connectionRef.current = client;
        }
      } catch (error) {
        console.error("Failed to fetch in-app service info:", error);
        setConnectionStatus("disconnected");
      } finally {
        isConnectingRef.current = false;
      }
    };

    fetchServiceInfoAndConnect();

    // Cleanup function to close the connection when the component unmounts or userId changes
    return () => {
      if (connectionRef.current) {
        const currentConn: any = connectionRef.current;
        if (typeof currentConn.deactivate === "function") {
          currentConn.deactivate();
        } else if (typeof currentConn.close === "function") {
          currentConn.close();
        }
        connectionRef.current = null;
        setConnectionStatus("disconnected");
      }
    };
    // Depend only on userId to avoid infinite loops
  }, [userId]);

  // Clear cache when user logs out
  useEffect(() => {
    if (!userId) {
      serviceInfoCache.clearCache();
      isConnectingRef.current = false;
    }
  }, [userId]);

  return (
    <RealtimeContext.Provider value={{ connectionStatus, lastMessage }}>
      {children}
    </RealtimeContext.Provider>
  );
};

export const useRealtime = () => {
  const context = useContext(RealtimeContext);
  if (context === undefined) {
    throw new Error("useRealtime must be used within a RealtimeProvider");
  }
  return context;
};
