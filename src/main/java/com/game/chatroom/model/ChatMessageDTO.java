package com.game.chatroom.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ChatMessageDTO {

    private String id;
    private String senderId;
    private List<String> recipientIds;
    private String content;

}
