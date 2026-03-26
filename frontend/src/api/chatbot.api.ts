import type { ApiResponse } from "./apiClient";
import { BaseApi } from "./base.api";
import type {
  Conversation,
  Message,
  RetrieveConversationListRequest,
  RetrieveMessageListRequest,
  SendMessageRequest,
  SendMessageResponse,
  StartConversationRequest,
} from "@/interfaces/chatbot.interface";

export class ChatbotApi extends BaseApi {
  constructor() {
    super("/services/catalog/api");
  }

  async retrieveConversationList(
    request: RetrieveConversationListRequest,
  ): Promise<ApiResponse<Conversation[]>> {
    return this.post<Conversation[]>("/chatbot/conversations/list", request);
  }

  async startConversation(
    request: StartConversationRequest,
  ): Promise<ApiResponse<SendMessageResponse>> {
    return this.post<SendMessageResponse>("/chatbot/conversations/start", request);
  }

  async deleteConversation(conversationId: string): Promise<ApiResponse<void>> {
    return this.delete<void>(`/chatbot/conversations/${conversationId}`);
  }

  async retrieveMessageList(
    request: RetrieveMessageListRequest,
  ): Promise<ApiResponse<Message[]>> {
    return this.post<Message[]>("/chatbot/messages/list", request);
  }

  async sendMessage(
    request: SendMessageRequest,
  ): Promise<ApiResponse<SendMessageResponse>> {
    return this.post<SendMessageResponse>("/chatbot/messages/send", request);
  }
}

export const chatbotApi = new ChatbotApi();
