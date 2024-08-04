package com.game.monopoly.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Document
public class Game {
    @Id
    private String gameId;
    private List<String> playerIdList;
    private List<Integer> playerMoneyList;
    private List<Boolean> isPlayerStuckList;
    private List<Boolean> playerStatusList;
    private String currentPlayer;
    private String status;
    private Date startTime;
    private int noOfPropertiesOnBoard;
    private Map<String, Token> playerTokenMap; // playerid vs token
    private Map<String, List<String>> propertyOwnerMap; // playerid who owns it to list of owned properties
    private int dice1Value;
    private int dice2Value;
}
