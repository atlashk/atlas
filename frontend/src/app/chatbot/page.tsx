"use client";

import { chatbotApi } from "@/api/chatbot.api";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Spinner } from "@/components/ui/spinner";
import { Textarea } from "@/components/ui/textarea";
import { withAuth } from "@/hoc/withAuth";
import type { Conversation, Message } from "@/interfaces/chatbot.interface";
import { cn } from "@/lib/utils";
import { Bot, BotMessageSquare, MoreHorizontal, Send, Trash2, User } from "lucide-react";
import { useCallback, useEffect, useMemo, useRef, useState, type UIEvent } from "react";
import { toast } from "sonner";

const CONVERSATION_PAGE_SIZE = 20;
const MESSAGE_PAGE_SIZE = 100;

type UiMessage = Message;

function ChatbotPage() {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [conversationPage, setConversationPage] = useState(1);
  const [hasMoreConversations, setHasMoreConversations] = useState(true);
  const [isLoadingConversations, setIsLoadingConversations] = useState(false);
  const [isLoadingMoreConversations, setIsLoadingMoreConversations] = useState(false);
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<UiMessage[]>([]);
  const [messagePage, setMessagePage] = useState(1);
  const [hasMoreMessages, setHasMoreMessages] = useState(true);
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isLoadingMoreMessages, setIsLoadingMoreMessages] = useState(false);
  const [messageInput, setMessageInput] = useState("");
  const [isSendingMessage, setIsSendingMessage] = useState(false);
  const [conversationToDelete, setConversationToDelete] = useState<Conversation | null>(null);
  const [isDeletingConversation, setIsDeletingConversation] = useState(false);
  const messageContainerRef = useRef<HTMLDivElement | null>(null);
  const selectedConversationIdRef = useRef<string | null>(null);
  const latestMessageRequestRef = useRef(0);

  const selectedConversation = useMemo(
    () => conversations.find((conversation) => conversation.id === selectedConversationId) || null,
    [conversations, selectedConversationId],
  );

  const formatTimestamp = useCallback((value: string) => {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return "";
    }
    return date.toLocaleString();
  }, []);

  const sortMessagesByCreatedAtAsc = useCallback((list: UiMessage[]) => {
    return [...list].sort((firstMessage, secondMessage) => {
      const firstTimestamp = new Date(firstMessage.createdAt).getTime();
      const secondTimestamp = new Date(secondMessage.createdAt).getTime();
      if (Number.isNaN(firstTimestamp) || Number.isNaN(secondTimestamp)) {
        return 0;
      }
      return firstTimestamp - secondTimestamp;
    });
  }, []);

  const retrieveConversations = useCallback(
    async (page: number, append: boolean) => {
      if (append) {
        setIsLoadingMoreConversations(true);
      } else {
        setIsLoadingConversations(true);
      }
      try {
        const response = await chatbotApi.retrieveConversationList({
          page,
          size: CONVERSATION_PAGE_SIZE,
        });
        if (!response.success || !response.data) {
          toast.error(response.errorMessage || "Failed to load conversations");
          return;
        }
        const newConversations = response.data;
        setConversations((previous) =>
          append ? [...previous, ...newConversations.filter((item) => !previous.some((existing) => existing.id === item.id))] : newConversations,
        );
        setHasMoreConversations(newConversations.length === CONVERSATION_PAGE_SIZE);
        setConversationPage(page);
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : "Failed to load conversations";
        toast.error(errorMessage);
      } finally {
        setIsLoadingConversations(false);
        setIsLoadingMoreConversations(false);
      }
    },
    [],
  );

  const scrollMessagesToBottom = useCallback(() => {
    const container = messageContainerRef.current;
    if (!container) {
      return;
    }
    container.scrollTop = container.scrollHeight;
  }, []);

  const retrieveMessages = useCallback(async (conversationId: string, page: number, appendOlder: boolean) => {
    const requestId = latestMessageRequestRef.current + 1;
    latestMessageRequestRef.current = requestId;
    const isLatestRequest = () =>
      latestMessageRequestRef.current === requestId &&
      selectedConversationIdRef.current === conversationId;

    if (appendOlder) {
      setIsLoadingMoreMessages(true);
    } else {
      setIsLoadingMessages(true);
    }
    try {
      const response = await chatbotApi.retrieveMessageList({
        conversationId,
        page,
        size: MESSAGE_PAGE_SIZE,
      });
      if (!response.success || !response.data) {
        if (!isLatestRequest()) {
          return;
        }
        toast.error(response.errorMessage || "Failed to load messages");
        if (!appendOlder) {
          setMessages([]);
          setMessagePage(1);
          setHasMoreMessages(false);
        }
        return;
      }
      if (!isLatestRequest()) {
        return;
      }
      const normalizedMessages = sortMessagesByCreatedAtAsc(response.data);
      if (appendOlder) {
        const container = messageContainerRef.current;
        const previousScrollHeight = container?.scrollHeight ?? 0;
        const previousScrollTop = container?.scrollTop ?? 0;
        setMessages((previous) => {
          const previousIds = new Set(previous.map((message) => message.id));
          const uniqueOlderMessages = normalizedMessages.filter(
            (message) => !previousIds.has(message.id),
          );
          return sortMessagesByCreatedAtAsc([...uniqueOlderMessages, ...previous]);
        });
        requestAnimationFrame(() => {
          const currentContainer = messageContainerRef.current;
          if (!currentContainer) {
            return;
          }
          const scrollDelta = currentContainer.scrollHeight - previousScrollHeight;
          currentContainer.scrollTop = previousScrollTop + scrollDelta;
        });
      } else {
        setMessages(normalizedMessages);
        requestAnimationFrame(() => {
          scrollMessagesToBottom();
        });
      }
      setMessagePage(page);
      setHasMoreMessages(response.data.length === MESSAGE_PAGE_SIZE);
    } catch (error) {
      if (!isLatestRequest()) {
        return;
      }
      const errorMessage =
        error instanceof Error ? error.message : "Failed to load messages";
      toast.error(errorMessage);
      if (!appendOlder) {
        setMessages([]);
        setMessagePage(1);
        setHasMoreMessages(false);
      }
    } finally {
      if (!isLatestRequest()) {
        return;
      }
      setIsLoadingMessages(false);
      setIsLoadingMoreMessages(false);
    }
  }, [scrollMessagesToBottom, sortMessagesByCreatedAtAsc]);

  const loadInitialData = useCallback(async () => {
    await retrieveConversations(1, false);
  }, [retrieveConversations]);

  useEffect(() => {
    void loadInitialData();
  }, [loadInitialData]);

  useEffect(() => {
    selectedConversationIdRef.current = selectedConversationId;
  }, [selectedConversationId]);

  useEffect(() => {
    latestMessageRequestRef.current += 1;
    if (!selectedConversationId) {
      setMessages([]);
      setMessagePage(1);
      setHasMoreMessages(false);
      setIsLoadingMessages(false);
      setIsLoadingMoreMessages(false);
      return;
    }
    setMessagePage(1);
    setHasMoreMessages(true);
    void retrieveMessages(selectedConversationId, 1, false);
  }, [selectedConversationId, retrieveMessages]);

  const handleLoadMoreMessages = useCallback(async () => {
    if (
      !selectedConversationId ||
      !hasMoreMessages ||
      isLoadingMessages ||
      isLoadingMoreMessages
    ) {
      return;
    }
    await retrieveMessages(selectedConversationId, messagePage + 1, true);
  }, [
    hasMoreMessages,
    isLoadingMessages,
    isLoadingMoreMessages,
    messagePage,
    retrieveMessages,
    selectedConversationId,
  ]);

  const handleMessageScroll = useCallback(
    async (event: UIEvent<HTMLDivElement>) => {
      const target = event.currentTarget;
      if (target.scrollTop < 120) {
        await handleLoadMoreMessages();
      }
    },
    [handleLoadMoreMessages],
  );

  const handleLoadMoreConversations = useCallback(async () => {
    if (!hasMoreConversations || isLoadingMoreConversations || isLoadingConversations) {
      return;
    }
    await retrieveConversations(conversationPage + 1, true);
  }, [
    conversationPage,
    hasMoreConversations,
    isLoadingConversations,
    isLoadingMoreConversations,
    retrieveConversations,
  ]);

  const handleConversationScroll = useCallback(
    async (event: UIEvent<HTMLDivElement>) => {
      const target = event.currentTarget;
      const distanceToBottom =
        target.scrollHeight - target.scrollTop - target.clientHeight;
      if (distanceToBottom < 120) {
        await handleLoadMoreConversations();
      }
    },
    [handleLoadMoreConversations],
  );

  const handleDeleteConversation = useCallback(async () => {
    if (!conversationToDelete) {
      return;
    }
    setIsDeletingConversation(true);
    try {
      const response = await chatbotApi.deleteConversation(conversationToDelete.id);
      if (!response.success) {
        toast.error(response.errorMessage || "Failed to delete conversation");
        return;
      }
      setConversations((previous) =>
        previous.filter((conversation) => conversation.id !== conversationToDelete.id),
      );
      if (selectedConversationId === conversationToDelete.id) {
        setSelectedConversationId(null);
        setMessages([]);
      }
      toast.success("Conversation deleted");
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to delete conversation";
      toast.error(errorMessage);
    } finally {
      setIsDeletingConversation(false);
      setConversationToDelete(null);
    }
  }, [conversationToDelete, selectedConversationId]);

  const handleSendMessage = useCallback(async () => {
    const content = messageInput.trim();
    if (!content || isSendingMessage) {
      return;
    }

    setIsSendingMessage(true);
    setMessageInput("");
    try {
      if (!selectedConversationId) {
        const startResponse = await chatbotApi.startConversation({
          messageType: "TEXT",
          text: content,
        });
        if (!startResponse.success || !startResponse.data) {
          toast.error(startResponse.errorMessage || "Failed to start conversation");
          setMessageInput(content);
          return;
        }
        const newConversationId = startResponse.data.conversationId;
        if (!newConversationId) {
          toast.error("Missing conversation id from start conversation response");
          setMessageInput(content);
          return;
        }

        const messageCreatedAt = startResponse.data.createdAt || new Date().toISOString();
        const conversationTitle = content.length > 50 ? `${content.slice(0, 50)}...` : content;
        const newConversation: Conversation = {
          id: newConversationId,
          title: conversationTitle,
          createdAt: messageCreatedAt,
          updatedAt: messageCreatedAt,
        };

        setConversations((previous) => [
          newConversation,
          ...previous.filter((conversation) => conversation.id !== newConversationId),
        ]);
        setSelectedConversationId(newConversationId);

        const userMessage: UiMessage = {
          id: `temp-user-${Date.now()}`,
          conversationId: newConversationId,
          messageType: "TEXT",
          senderType: "USER",
          text: content,
          createdAt: new Date().toISOString(),
        };

        const assistantMessage: UiMessage = {
          id: `temp-assistant-${Date.now()}`,
          conversationId: newConversationId,
          messageType: "TEXT",
          senderType: "ASSISTANT",
          text: startResponse.data.text,
          createdAt: messageCreatedAt,
        };

        setMessages([userMessage, assistantMessage]);
        requestAnimationFrame(() => {
          scrollMessagesToBottom();
        });
        return;
      }

      const temporaryUserMessage: UiMessage = {
        id: `temp-user-${Date.now()}`,
        conversationId: selectedConversationId,
        messageType: "TEXT",
        senderType: "USER",
        text: content,
        createdAt: new Date().toISOString(),
      };

      setMessages((previous) => [...previous, temporaryUserMessage]);
      requestAnimationFrame(() => {
        scrollMessagesToBottom();
      });

      const response = await chatbotApi.sendMessage({
        conversationId: selectedConversationId,
        messageType: "TEXT",
        text: content,
      });

      if (!response.success || !response.data) {
        toast.error(response.errorMessage || "Failed to send message");
        setMessages((previous) => previous.filter((message) => message.id !== temporaryUserMessage.id));
        setMessageInput(content);
        return;
      }

      const assistantMessage: UiMessage = {
        id: `temp-assistant-${Date.now()}`,
        conversationId: selectedConversationId,
        messageType: "TEXT",
        senderType: "ASSISTANT",
        text: response.data.text,
        createdAt: response.data.createdAt || new Date().toISOString(),
      };

      setMessages((previous) => [...previous, assistantMessage]);
      requestAnimationFrame(() => {
        scrollMessagesToBottom();
      });
      await retrieveMessages(selectedConversationId, 1, false);
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : "Failed to send message";
      toast.error(errorMessage);
      setMessageInput(content);
    } finally {
      setIsSendingMessage(false);
    }
  }, [
    isSendingMessage,
    messageInput,
    retrieveMessages,
    scrollMessagesToBottom,
    selectedConversationId,
  ]);

  return (
    <div className="container mx-auto px-4 py-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BotMessageSquare className="h-5 w-5" />
            <span>Chatbot Assistant</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-4 lg:grid-cols-[minmax(340px,380px)_1fr]">
            <div className="border rounded-md">
              <div className="border-b px-4 py-3 font-medium">Conversations</div>
              <ScrollArea className="h-[70vh]">
                <div onScroll={handleConversationScroll} className="h-full overflow-y-auto">
                  {isLoadingConversations ? (
                    <div className="flex h-32 items-center justify-center">
                      <Spinner className="text-blue-600 h-5 w-5" />
                    </div>
                  ) : conversations.length === 0 ? (
                    <div className="px-4 py-8 text-center text-sm text-gray-500">
                      No conversations yet
                    </div>
                  ) : (
                    <div className="p-2 space-y-2">
                      {conversations.map((conversation) => (
                        <div
                          key={conversation.id}
                          className={cn(
                            "rounded-md border px-3 py-2",
                            selectedConversationId === conversation.id
                              ? "border-blue-500 bg-blue-50"
                              : "border-gray-200",
                          )}
                        >
                          <div className="flex items-start justify-between gap-2">
                            <button
                              type="button"
                              onClick={() => setSelectedConversationId(conversation.id)}
                              className="min-w-0 flex-1 text-left"
                            >
                              <p className="truncate text-sm font-medium">{conversation.title || "Untitled conversation"}</p>
                              <p className="mt-1 truncate text-xs text-gray-500">
                                {formatTimestamp(conversation.updatedAt || conversation.createdAt)}
                              </p>
                            </button>
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="icon" className="h-7 w-7 shrink-0">
                                  <MoreHorizontal className="h-4 w-4" />
                                </Button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent align="end">
                                <DropdownMenuItem
                                  className="cursor-pointer text-red-600"
                                  onSelect={() => setConversationToDelete(conversation)}
                                >
                                  <Trash2 className="mr-2 h-4 w-4" />
                                  Delete conversation
                                </DropdownMenuItem>
                              </DropdownMenuContent>
                            </DropdownMenu>
                          </div>
                        </div>
                      ))}
                      {isLoadingMoreConversations && (
                        <div className="flex justify-center py-2">
                          <Spinner className="text-blue-600 h-5 w-5" />
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </ScrollArea>
            </div>

            <div className="border rounded-md flex flex-col h-[70vh] min-h-0 overflow-hidden">
              <div className="border-b px-4 py-3 font-medium">
                {selectedConversation?.title || "New conversation"}
              </div>
              <div
                ref={messageContainerRef}
                onScroll={handleMessageScroll}
                className="flex-1 min-h-0 overflow-y-auto px-4 py-3 space-y-3"
              >
                  {isLoadingMessages ? (
                    <div className="flex h-20 items-center justify-center">
                      <Spinner className="text-blue-600 h-5 w-5" />
                    </div>
                  ) : messages.length === 0 ? (
                    <div className="flex h-full min-h-40 items-center justify-center text-sm text-gray-500">
                      Start the conversation by sending your message
                    </div>
                  ) : (
                    messages.map((message) => {
                      const isAssistant = message.senderType === "ASSISTANT";
                      return (
                        <div
                          key={message.id}
                          className={cn(
                            "flex gap-2",
                            isAssistant ? "justify-start" : "justify-end",
                          )}
                        >
                          {isAssistant && (
                            <div className="h-8 w-8 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center">
                              <Bot className="h-4 w-4" />
                            </div>
                          )}
                          <div
                            className={cn(
                              "max-w-[75%] rounded-lg px-3 py-2 text-sm",
                              isAssistant
                                ? "bg-gray-100 text-gray-900"
                                : "bg-blue-600 text-white",
                            )}
                          >
                            <p className="whitespace-pre-wrap break-words">{message.text}</p>
                            <p
                              className={cn(
                                "mt-1 text-[11px]",
                                isAssistant ? "text-gray-500" : "text-blue-100",
                              )}
                            >
                              {formatTimestamp(message.createdAt)}
                            </p>
                          </div>
                          {!isAssistant && (
                            <div className="h-8 w-8 rounded-full bg-blue-600 text-white flex items-center justify-center">
                              <User className="h-4 w-4" />
                            </div>
                          )}
                        </div>
                      );
                    })
                  )}
                  {isLoadingMoreMessages && !isLoadingMessages && (
                    <div className="flex justify-center py-2">
                      <Spinner className="text-blue-600 h-5 w-5" />
                    </div>
                  )}
              </div>
              <div className="border-t p-3">
                <div className="flex items-end gap-2">
                  <Textarea
                    value={messageInput}
                    onChange={(event) => setMessageInput(event.target.value)}
                    placeholder="Type your message..."
                    className="min-h-[56px] max-h-40"
                    onKeyDown={(event) => {
                      if (event.key === "Enter" && !event.shiftKey) {
                        event.preventDefault();
                        void handleSendMessage();
                      }
                    }}
                  />
                  <Button
                    onClick={() => void handleSendMessage()}
                    disabled={isSendingMessage || !messageInput.trim()}
                    className="h-10"
                  >
                    {isSendingMessage ? (
                      <Spinner className="h-4 w-4 mr-2" />
                    ) : (
                      <Send className="h-4 w-4 mr-2" />
                    )}
                    Send
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <AlertDialog
        open={!!conversationToDelete}
        onOpenChange={(open) => {
          if (!open) {
            setConversationToDelete(null);
          }
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete conversation</AlertDialogTitle>
            <AlertDialogDescription>
              This action will permanently remove the conversation and all its messages.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isDeletingConversation}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={(event) => {
                event.preventDefault();
                void handleDeleteConversation();
              }}
              disabled={isDeletingConversation}
              className="bg-red-600 hover:bg-red-700"
            >
              {isDeletingConversation ? <Spinner className="h-4 w-4 mr-2" /> : null}
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

export default withAuth(ChatbotPage);
