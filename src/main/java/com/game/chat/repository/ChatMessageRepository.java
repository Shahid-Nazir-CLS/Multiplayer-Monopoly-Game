package com.game.chat.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.game.chat.model.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomId(String chatId);

    @Query(value = "{ 'gameId': 'GLOBAL' }", sort = "{ 'timestamp': 1 }")
    List<ChatMessage> findTop50ByChatRoomIdAndGameIdGlobal(Sort sort);
}