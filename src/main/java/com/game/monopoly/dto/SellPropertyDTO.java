package com.game.monopoly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellPropertyDTO {
    private String gameId;
    private String playerId; 
    private String propertyName; 
}
