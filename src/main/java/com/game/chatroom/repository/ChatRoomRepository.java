package com.game.chatroom.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.game.chatroom.model.ChatRoom;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    @Query("{ 'senderId': ?0, 'recipientId': ?1 }")
    Optional<ChatRoom> findBySenderIdAndRecipientId(String senderId, String recipientId);

    Optional<ChatRoom> findByGameId(String gameId);
}