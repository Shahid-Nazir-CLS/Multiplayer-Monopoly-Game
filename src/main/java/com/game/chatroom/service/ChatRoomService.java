package com.game.chatroom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.chatroom.model.ChatRoom;
import com.game.chatroom.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public String getChatRoomForGame(String gameId){
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByGameId(gameId);

        // If found, return the chat room ID
        if (chatRoomOpt.isPresent()) {
            return chatRoomOpt.get().getId();
        }

        // If not found, create a new chat room
        String chatRoomId = createChatRoom(gameId);
        return chatRoomId;
    }

    private String createChatRoom(String gameId) {
        ChatRoom chatRoom = new ChatRoom(null, gameId);
        chatRoomRepository.save(chatRoom);
        return chatRoom.getId();
    }
}