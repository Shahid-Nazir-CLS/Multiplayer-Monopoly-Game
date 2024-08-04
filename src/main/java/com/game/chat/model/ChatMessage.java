package com.game.chat.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class ChatMessage {
    @Id
    private String id;
    private String chatRoomId;
    private String senderId;
    private List<String> recipientIds;
    private String content;
    private String gameId;
    private Date timestamp;
}