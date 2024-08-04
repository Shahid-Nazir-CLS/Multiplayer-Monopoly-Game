package com.game.chatroom.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.chat.model.ChatMessage;
import com.game.chat.service.ChatMessageService;
import com.game.chatroom.model.ChatMessageDTO;
import com.game.player.service.PlayerService;

@Controller
public class ChatController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private PlayerService playerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Handles chat messages sent to the "/chat" endpoint
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        System.out.println("Received message: " + chatMessage);
        
        // Save the received chat message
        ChatMessage savedMsg = chatMessageService.save(chatMessage);
        
        // Create a DTO (Data Transfer Object) for the saved message
        ChatMessageDTO recipientMsg = new ChatMessageDTO(savedMsg.getId(), savedMsg.getSenderId(), savedMsg.getRecipientIds(), savedMsg.getContent());
        
        // Send the message to each recipient
        for (String recipientId : savedMsg.getRecipientIds()) {
            messagingTemplate.convertAndSendToUser(recipientId, "/queue/messages", recipientMsg);
        }
    }

    // Handles global chat messages sent to the "/global/chat" endpoint
    @MessageMapping("/global/chat")
    public void processGlobalMessage(@Payload ChatMessage chatMessage) {
        System.out.println("Received message: " + chatMessage);
        
        // Save the received global chat message
        ChatMessage savedMsg = chatMessageService.save(chatMessage);
        
        // Get all online player IDs
        List<String> recipientIds = playerService.getAllOnlinePlayerIds();
        
        // Send the message to each online player
        for (String recipientJson : recipientIds) {
            try {
                // Parse the JSON string to extract the nickname
                JsonNode jsonNode = objectMapper.readTree(recipientJson);
                String nickName = jsonNode.get("nickName").asText();
                
                // Send the message to the user (assuming nickName is used as the user destination)
                messagingTemplate.convertAndSendToUser(nickName, "/queue/globalMessages", savedMsg);
            } catch (IOException e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
            }
        }
    }

    // Endpoint to get chat messages for a specific game
    @GetMapping("/messages/{gameId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable("gameId") String gameId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(gameId));
    }

    // Endpoint to get global chat messages
    @GetMapping("/global/messages")
    public ResponseEntity<List<ChatMessage>> findGlobalChatMessages() {
        System.out.println(chatMessageService.findGlobalChatMessages());
        return ResponseEntity.ok(chatMessageService.findGlobalChatMessages());
    }
}
