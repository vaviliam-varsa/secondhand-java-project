package com.secondhand.frontend.service;

import com.secondhand.frontend.http.ApiClient;
import com.secondhand.frontend.http.ApiException;
import com.secondhand.frontend.model.ChatMessage;
import com.secondhand.frontend.model.Conversation;
import com.secondhand.frontend.model.SendMessageRequest;
import com.secondhand.frontend.model.StartConversationRequest;
import com.secondhand.frontend.model.StartConversationResponse;
import com.secondhand.frontend.util.JsonUtil;

import java.util.List;

public class ChatService {

    public static List<Conversation> listConversations() throws ApiException {
        String json = ApiClient.get("/api/conversations", true);
        return JsonUtil.fromJsonList(json, Conversation.class);
    }

    public static List<ChatMessage> listMessages(long conversationId) throws ApiException {
        String json = ApiClient.get("/api/conversations/" + conversationId + "/messages", true);
        return JsonUtil.fromJsonList(json, ChatMessage.class);
    }

    /** Starts a new conversation (or reuses an existing one, per backend logic) by sending the first message. */
    public static Long startConversation(long advertisementId, String content) throws ApiException {
        StartConversationRequest req = new StartConversationRequest();
        req.advertisementId = advertisementId;
        req.content = content;
        String json = ApiClient.post("/api/conversations", req, true);
        StartConversationResponse resp = JsonUtil.fromJson(json, StartConversationResponse.class);
        return resp.conversationId;
    }

    public static void sendMessage(long conversationId, String content) throws ApiException {
        SendMessageRequest req = new SendMessageRequest();
        req.content = content;
        ApiClient.post("/api/conversations/" + conversationId + "/messages", req, true);
    }
}