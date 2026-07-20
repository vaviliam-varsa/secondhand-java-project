package com.example.secondhandbackend.repository;

import com.example.secondhandbackend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByBuyerIdOrSellerId(Long buyerId, Long sellerId);

    Optional<Conversation> findByAdvertisementIdAndBuyerIdAndSellerId(
            Long advertisementId, Long buyerId, Long sellerId);
}