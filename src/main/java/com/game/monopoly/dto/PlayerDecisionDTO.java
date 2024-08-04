package com.game.monopoly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDecisionDTO {
    private String gameId;
    private String playerId;
    private boolean propertyBought;
    private boolean rentPaid;
    private int propertyLocX;
    private int propertyLocY;
}
     