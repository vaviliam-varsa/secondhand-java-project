package com.example.secondhandbackend.controller;

import com.example.secondhandbackend.dto.*;
import com.example.secondhandbackend.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<ConversationListItemResponse> getConversations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return conversationService.getUserConversations(userId);
    }

    @GetMapping("/{id}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return conversationService.getMessages(id, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StartConversationResponse startConversation(@RequestBody StartConversationRequest request,
                                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long conversationId = conversationService.startConversation(
                userId, request.getAdvertisementId(), request.getContent());
        return new StartConversationResponse(conversationId, "Message sent");
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public com.example.secondhandbackend.dto.MessageResponse sendMessage(
            @PathVariable Long id, @RequestBody SendMessageRequest request, Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        conversationService.sendMessage(id, userId, request.getContent());
        return new com.example.secondhandbackend.dto.MessageResponse("Message sent");
    }
}