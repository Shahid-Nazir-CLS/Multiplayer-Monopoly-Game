package com.game.chat.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.game.chat.model.ChatMessage;
import com.game.chat.repository.ChatMessageRepository;
import com.game.chatroom.service.ChatRoomService;

@Service
public class ChatMessageService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessage chatMessage) {
        String chatId = chatRoomService.getChatRoomForGame(chatMessage.getGameId());
        chatMessage.setChatRoomId(chatId);
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> findChatMessages(String gameId) {
        String chatRoomId = chatRoomService.getChatRoomForGame(gameId);
        if (chatRoomId != null) {
            // If found, return messages for this chat room
            return chatMessageRepository.findByChatRoomId(chatRoomId);
        }

        // If no chat room found in either case, return an empty list
        return new ArrayList<>();
    }

    public List<ChatMessage> findGlobalChatMessages() {
        return chatMessageRepository.findTop50ByChatRoomIdAndGameIdGlobal( Sort.by(Sort.Direction.ASC, "timestamp"));
    }
}
