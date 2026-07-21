package com.example.secondhandbackend.service;

import com.example.secondhandbackend.dto.ChatMessageResponse;
import com.example.secondhandbackend.dto.ConversationListItemResponse;
import com.example.secondhandbackend.entity.Advertisement;
import com.example.secondhandbackend.entity.Conversation;
import com.example.secondhandbackend.entity.Message;
import com.example.secondhandbackend.entity.User;
import com.example.secondhandbackend.exception.AccessDeniedException;
import com.example.secondhandbackend.exception.DuplicateResourceException;
import com.example.secondhandbackend.exception.ResourceNotFoundException;
import com.example.secondhandbackend.repository.AdvertisementRepository;
import com.example.secondhandbackend.repository.ConversationRepository;
import com.example.secondhandbackend.repository.MessageRepository;
import com.example.secondhandbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               AdvertisementRepository advertisementRepository,
                               UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
    }

    public List<ConversationListItemResponse> getUserConversations(Long userId) {

        List<Conversation> conversations = conversationRepository.findByBuyerIdOrSellerId(userId, userId);

        return conversations.stream()
                .map(conv -> {
                    User otherUser = conv.getBuyer().getId().equals(userId) ? conv.getSeller() : conv.getBuyer();

                    List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conv.getId());
                    Message lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);

                    return new ConversationListItemResponse(
                            conv.getId(),
                            new ConversationListItemResponse.AdSummary(
                                    conv.getAdvertisement().getId(), conv.getAdvertisement().getTitle()),
                            new ConversationListItemResponse.UserSummary(
                                    otherUser.getId(), otherUser.getFullName()),
                            lastMessage != null ? lastMessage.getContent() : null,
                            lastMessage != null ? lastMessage.getSentAt() : null
                    );
                })
                .toList();
    }

    public List<ChatMessageResponse> getMessages(Long conversationId, Long userId) {

        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conv.getBuyer().getId().equals(userId) && !conv.getSeller().getId().equals(userId)) {
            throw new AccessDeniedException("You are not part of this conversation");
        }

        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        return messages.stream()
                .map(m -> new ChatMessageResponse(m.getId(), m.getContent(), m.getSentAt(), m.getSender().getId()))
                .toList();
    }

    public Long startConversation(Long buyerId, Long advertisementId, String content) {

        Advertisement ad = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new ResourceNotFoundException("Advertisement not found"));

        if (ad.getOwner().getId().equals(buyerId)) {
            throw new DuplicateResourceException("You cannot message yourself about your own advertisement");
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long sellerId = ad.getOwner().getId();

        Optional<Conversation> existing = conversationRepository
                .findByAdvertisementIdAndBuyerIdAndSellerId(advertisementId, buyerId, sellerId);

        Conversation conversation = existing.orElseGet(() -> {
            Conversation newConv = new Conversation();
            newConv.setAdvertisement(ad);
            newConv.setBuyer(buyer);
            newConv.setSeller(ad.getOwner());
            return conversationRepository.save(newConv);
        });

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(buyer);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        messageRepository.save(message);

        return conversation.getId();
    }

    public void sendMessage(Long conversationId, Long senderId, String content) {

        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conv.getBuyer().getId().equals(senderId) && !conv.getSeller().getId().equals(senderId)) {
            throw new AccessDeniedException("You are not part of this conversation");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Message message = new Message();
        message.setConversation(conv);
        message.setSender(sender);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());

        messageRepository.save(message);
    }
}