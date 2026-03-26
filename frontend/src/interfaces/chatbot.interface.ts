export interface RetrieveConversationListRequest {
  page: number;
  size: number;
}

export interface Conversation {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface StartConversationRequest {
  firstMessage: string;
}

export interface RetrieveMessageListRequest {
  conversationId: string;
  page: number;
  size: number;
}

export interface SendMessageRequest {
  conversationId: string;
  messageType: string;
  text: string;
}

export interface Message {
  id: string;
  conversationId: string;
  messageType: string;
  senderType: string;
  text: string;
  createdAt: string;
}

export interface SendMessageResponse {
  conversationId?: string;
  text: string;
  createdAt: string;
}
