package com.game.monopoly.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.swing.JOptionPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.game.chatroom.model.ChatRoom;
import com.game.chatroom.repository.ChatRoomRepository;
import com.game.monopoly.dto.PlayerDecisionDTO;
import com.game.monopoly.dto.SellPropertyDTO;
import com.game.monopoly.model.Game;
import com.game.monopoly.model.GameTextMessage;
import com.game.monopoly.model.Property;
import com.game.monopoly.model.Token;
import com.game.monopoly.repository.GameRepository;
import com.game.player.model.Player;
import com.oracle.wls.shaded.org.apache.xpath.operations.Bool;




@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private GameTextMessageService gameTextMessageService;

    @Autowired
    private PropertyService propertyService;

    // normal boxes increment
    private final int inc1 = 64;
    // bottom left corner to up increment
    private final int inc2 = 80;
    // bottom right corner to left increment and top right -1 to top right increment
    private final int inc3 = 102;

    private final List<List<Integer>> initialPositions = Arrays.asList(
        Arrays.asList(770, 703),  // Coordinates for token1
        Arrays.asList(770, 720),  // Coordinates for token2
        Arrays.asList(770, 737),  // Coordinates for token3
        Arrays.asList(770, 754)   // Coordinates for token4
    );

    public List<Game> findAllPlayerGames(String userId){
        Sort sort = Sort.by(Sort.Order.desc("startTime"));
        return gameRepository.findByPlayerIdList(userId, sort);
    }

    public Game findGameById(String gameId){
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if(gameOpt.isPresent()){
            return gameOpt.get();
        }
        return null;
    }

    /*public void createDummyGameData(){
        for(int i = 0; i < 20; i++){

            List<String> playerNickNames = Arrays.asList("test1", "test2", "test3", "test4");
            Random random = new Random();
            String playerNickName = playerNickNames.get(random.nextInt(playerNickNames.size()));
            List<Game> games = gameRepository.findAll();
            Game game = games.get(0);

            game.setGameId(null);
            game.setStatus(playerNickName + " WINS");
            gameRepository.save(game);
            System.out.println(game);
        }
    }*/

    public Game createNewGame(List<Player> players){

        List<Integer> playerMoneyList = new ArrayList<>();
        List<String> playersIdList = new ArrayList<>();
        List<Boolean> isPlayerStuckList = new ArrayList<>();
        List<Boolean> playerStatusList = new ArrayList<>();
        Map<String, Token> playerTokenMap = new HashMap<>();
        Map<String, List<String>> propertyOwnerMap = new HashMap<>();

        Random random = new Random();

        for (Player player : players) {
            playerMoneyList.add(1500); // Initial money
            isPlayerStuckList.add(false); // Not stuck
            playerStatusList.add(true); // Active player
            playersIdList.add(player.getNickName());

            // propertyownermap init with empty lists
            List<String> props = new ArrayList<>();
            propertyOwnerMap.put(player.getNickName(), props);
        }

        // Set the start time to the current date and time
        Date startTime = new Date();

        // Initialize player tokens with random colors
        List<String> tokenColors = Arrays.asList("LAWNGREEN", "YELLOW", "AQUA", "SALMON");
        Collections.shuffle(tokenColors, random);

        for (int i = 0; i < players.size(); i++) {
            String playerId = players.get(i).getNickName();
            String tokenColor = tokenColors.get(i);
            playerTokenMap.put(playerId, new Token(4, 0, 0, tokenColor, initialPositions.get(i)));
        }

         // Select a random current player
        String currentPlayer = players.get(random.nextInt(players.size())).getNickName();

        
        Game game = Game.builder()
            .playerIdList(playersIdList)
            .playerMoneyList(playerMoneyList)
            .isPlayerStuckList(isPlayerStuckList)
            .playerStatusList(playerStatusList)
            .currentPlayer(currentPlayer)
            .status("IN PROGRESS")
            .startTime(startTime)
            .noOfPropertiesOnBoard(0)
            .playerTokenMap(playerTokenMap)
            .propertyOwnerMap(propertyOwnerMap)
            .dice1Value(5)
            .dice2Value(6)
            .build();

        game = gameRepository.save(game);

        // create chatroom for game
        ChatRoom chatRoom = new ChatRoom(null, game.getGameId());
        chatRoomRepository.save(chatRoom);

        // create messages
        gameTextMessageService.clearGameMessages();

        for(String playerId : game.getPlayerIdList()){
            // add game messages
            String playerMessage = playerId + " has joined the Game with " + game.getPlayerTokenMap().get(playerId).getTokenColor() + " token.";
            GameTextMessage msg = new GameTextMessage(null, game.getGameId(), playerMessage);
            
            // Add the message to the list
            gameTextMessageService.addGamMessages(msg);
        }

        // add current player turn
        GameTextMessage msgTurn = new GameTextMessage(null, game.getGameId(), game.getCurrentPlayer() + " has to roll the die.");
        gameTextMessageService.addGamMessages(msgTurn);

        return game;
    }

    public Game makeMove(String gameId, String playerId) {
        // to capture gameMessages which can be returned to UI
        gameTextMessageService.clearGameMessages();
        
        // fetch game from db so we can update it
        Game game = findGameById(gameId);

        // get player index
        int playerIndex = game.getPlayerIdList().indexOf(playerId);

        // we will check if player was stuck at current token location where he is at before the move
        // if he is not stuck and has money > 0 then we move him to next location according to die and there will check if he is again stuck or not
        // if he was stuck at that place and now he has rent, we will deduct rent for that locaton and move him next location where we will decide if he is stuck again or he can pay rent or buy
        // else if he is stuck and cannot still pay rent, we will move to next player  
        Boolean isPlayerStuck = game.getIsPlayerStuckList().get(playerIndex);
        int locX = game.getPlayerTokenMap().get(playerId).getLocX();
        int locY = game.getPlayerTokenMap().get(playerId).getLocY();
        int playerMoney = game.getPlayerMoneyList().get(playerIndex);
        Property playerLocationProperty = propertyService.getPropertyByCoordinates(locX, locY);
        
        // if player is not stuck and has money to money to pay property
        // or if player was stuck and now has money to pay for property
        if(!isPlayerStuck || isPlayerStuck && playerMoney >= playerLocationProperty.getRent()){
             /*
             * move player token 
             */
            moveToken(game);

            // check if person landed on lucky cards then add or deduct money or make him stuck if he landed in jail
            if(checkLuckyCard(game, playerId)){
                processLuckyCard(game, playerId, playerIndex);
            }

        }else if (isPlayerStuck && playerMoney < playerLocationProperty.getRent()){ // else if player is stuck and has no money to pay for rent of property, he is still stuck
            GameTextMessage msgBroke = new GameTextMessage(null, game.getGameId(), playerId + " is 'BROKE' and cannot buy or pay rent of $ " + playerLocationProperty.getRent() + " for this property and cannot roll the Die.");
            gameTextMessageService.addGamMessages(msgBroke);
            switchPlayer(game, playerIndex, isPlayerStuck);
        }

        /** 
         * 
         * Change turn of player or not
         * If person landed on lucky card after throwing die then we will switch player as he doesnt need to take any decision
         * Money will be deducted or added to him automatically and we will move to next player
         * or he will be stuck if he cannot pay fine or tax
         */
        if(checkLuckyCard(game, playerId)){
            System.out.println("in lucky card");
            switchPlayer(game, playerIndex, isPlayerStuck);
        }else{ // if not luck card then we need to inform frontend to show decision prompt
            GameTextMessage msgDecision = new GameTextMessage(null, game.getGameId(), "TAKE DECISION");
            gameTextMessageService.addGamMessages(msgDecision);
        }
            
        gameRepository.save(game);
        return game;
    }

    public boolean checkLuckyCard(Game game, String playerId){
        int locX = game.getPlayerTokenMap().get(playerId).getLocX();
        int locY = game.getPlayerTokenMap().get(playerId).getLocY();
        String locXAndLocY = locX + ", " + locY;
        // Check if the location matches any lucky card location
        switch (locXAndLocY) {
            case "-7, 0":
            case "0, -4":
            case "-8, -10":
            case "-10, -7":
            case "0, -7":
            case "-2, 0":
            case "0, 0":
            case "-10, -10":
            case "-4, 0":
            case "0, -2":
            case "-10, 0":
            case "0, -10":
                return true;
            default:
                return false;
        }
    }

    public void processLuckyCard(Game game, String playerId, int playerIndex){
        int locX = game.getPlayerTokenMap().get(playerId).getLocX();
        int locY = game.getPlayerTokenMap().get(playerId).getLocY();
        int playerMoney = game.getPlayerMoneyList().get(playerIndex);
        String locXAndLocY = locX + ", " + locY;
        GameTextMessage msg = null;
        GameTextMessage msg2 = null;

        switch (locXAndLocY) {
            case "-7, 0":
            case "0, -4":
            case "-8, -10":
                msg = new GameTextMessage(null, game.getGameId(), playerId + ", you have reached Chance.\n Parking here is free and you have won $50.");
                game.getPlayerMoneyList().set(playerIndex, playerMoney + 50);
                break;
            case "-10, -7":
            case "0, -7":
            case "-2, 0":
                msg = new GameTextMessage(null, game.getGameId(), playerId + ", you have found Community Chest.\n Parking here is free and you have won $200.");
                game.getPlayerMoneyList().set(playerIndex, playerMoney + 200);
                break;
            case "0, 0":
            case "-10, -10":
                msg = new GameTextMessage(null, game.getGameId(), playerId + ", you have reached Free Parking so you can stay here without any rent.");
                break;
            case "-4, 0":
                msg = new GameTextMessage(null, game.getGameId(), playerId + ", you have reached Income Tax.\n You have to pay $200 as Income Tax.");
                if(playerMoney >= 200){
                    game.getPlayerMoneyList().set(playerIndex, playerMoney - 200);
                    msg2 = new GameTextMessage(null, game.getGameId(), playerId + " has paid income tax of $200.");
                }else{
                    // player is stuck as he cannot pay fine
                    game.getIsPlayerStuckList().set(playerIndex, true);
                    msg2 = new GameTextMessage(null, game.getGameId(), playerId + " is stuck at Income Tax as he cannot pay tax of $200.");
                }
                break;
            case "0, -2":
                msg = new GameTextMessage(null, game.getGameId(), playerId + ", you have reached Luxury Tax.\n You have to pay $100 as Luxury Tax.");
                if(playerMoney >= 100){
                    game.getPlayerMoneyList().set(playerIndex, playerMoney - 100);
                    msg2 = new GameTextMessage(null, game.getGameId(), playerId + " has paid luxury tax of $200.");
                }else{
                    // player is stuck as he cannot pay fine
                    game.getIsPlayerStuckList().set(playerIndex, true);
                    msg2 = new GameTextMessage(null, game.getGameId(), playerId + " is stuck at Luxury Tax as he cannot pay tax of $100.");
                }
                break;
            case "-10, 0":
                msg = new GameTextMessage(null, game.getGameId(), playerId + ", you have landed in Jail.\n You have to pay $300 to get out of Jail.");
                if(playerMoney >= 300){
                    game.getPlayerMoneyList().set(playerIndex, playerMoney - 300);
                    msg2 = new GameTextMessage(null, game.getGameId(), playerId + " has paid fine of $300 to jail.");
                }else{
                    // player is stuck as he cannot pay fine
                    game.getIsPlayerStuckList().set(playerIndex, true);
                    msg2 = new GameTextMessage(null, game.getGameId(), playerId + " is stuck at jail as he cannot pay fine of $300.");
                }
                break;
            case "0, -10":
                msg = new GameTextMessage(null, game.getGameId(), playerId + ", you have landed in Go To Jail.\nYou will be sent to jail and you have to pay $300 to get out of Jail.");
                if(playerMoney >= 300){
                    game.getPlayerMoneyList().set(playerIndex, playerMoney - 300);
                    msg2 = new GameTextMessage(null, game.getGameId(), playerId + " has paid fine of $300 to jail");
                }else{
                    // player is stuck as he cannot pay fine
                    game.getIsPlayerStuckList().set(playerIndex, true);
                    msg2 = new GameTextMessage(null, game.getGameId(), playerId + " is stuck at jail as he cannot pay fine of $300.");
                }
                // player[turnOfPlayer].token.setlocX(-10);
                // player[turnOfPlayer].token.setlocY(0);
                // player[turnOfPlayer].token.square.setLocation(92, player[turnOfPlayer].token.square.getY() + 672);
                // player[turnOfPlayer].token.setDirection(2);
                break;
        }
        if(msg != null) gameTextMessageService.addGamMessages(msg);
        if(msg2 != null) gameTextMessageService.addGamMessages(msg2);
    }

    public Game takeDecision(PlayerDecisionDTO playerDecisionDTO){
        String gameId = playerDecisionDTO.getGameId();
        String playerId = playerDecisionDTO.getPlayerId();

        // to capture gameMessages which can be returned to UI
        gameTextMessageService.clearGameMessages();
        GameTextMessage msgDecision = null;

        // fetch game from db so we can update it
        Game game = findGameById(gameId);
        
        Property currProperty = propertyService.getPropertyByCoordinates(playerDecisionDTO.getPropertyLocX(), playerDecisionDTO.getPropertyLocY());
        
        // get player index
        int playerIndex = game.getPlayerIdList().indexOf(playerId);
        int playerMoney = game.getPlayerMoneyList().get(playerIndex);

        // if property not bought and not rent paid and currentPlayer is owner of property -- can stay here for free
        if(!playerDecisionDTO.isPropertyBought() && !playerDecisionDTO.isRentPaid() && currProperty.getOwner().equals(playerId)){
            msgDecision = new GameTextMessage(null, game.getGameId(), playerId + " owns " + currProperty.getName() + " and will saty here for free.");

        // if property not bought and no rent paid and currentPlayer is not owner of property -- playerIsStuck
        }else if(!playerDecisionDTO.isPropertyBought() && !playerDecisionDTO.isRentPaid()){
            game.getIsPlayerStuckList().set(playerIndex, true);
            msgDecision = new GameTextMessage(null, game.getGameId(), playerId + " is 'BROKE' and cannot buy or pay rent of $ " + currProperty.getRent() + " and is stuck here unil he pays rent.");

        // if property bought then update in playerPropertyMap and reduce from player money
        }else if(playerDecisionDTO.isPropertyBought()){
            currProperty.setOwner(playerId);
            game.getPropertyOwnerMap().get(playerId).add(currProperty.getName());
            game.getPlayerMoneyList().set(playerIndex, playerMoney - currProperty.getCost());
            msgDecision = new GameTextMessage(null, game.getGameId(), playerId + " bought " + currProperty.getName() + " for $ " + currProperty.getCost() + ".");

        // if rent paid then reduce from players money and check if player has owner then give him that rent
        }else if(playerDecisionDTO.isRentPaid()){
            game.getPlayerMoneyList().set(playerIndex, playerMoney - currProperty.getRent());
            msgDecision = new GameTextMessage(null, game.getGameId(), playerId + " paid rent of $ " + currProperty.getRent() + " for " + currProperty.getName() + ".");
        
            // Find the owner of the property
            for (Map.Entry<String, List<String>> entry : game.getPropertyOwnerMap().entrySet()) {
                String ownerId = entry.getKey();
                List<String> ownedProperties = entry.getValue();

                if (ownedProperties.contains(currProperty.getName())) {
                    // Property is owned by this ownerId
                    int ownerIndex = game.getPlayerIdList().indexOf(ownerId);
                    int ownerMoney = game.getPlayerMoneyList().get(ownerIndex);

                    // Add rent to the owner's money
                    game.getPlayerMoneyList().set(ownerIndex, ownerMoney + currProperty.getRent());
                    msgDecision = new GameTextMessage(null, game.getGameId(), playerId + " paid rent of $ " + currProperty.getRent() + " for " + currProperty.getName() + " to " + ownerId + ".");
                    break; // Exit the loop once the owner is found
                }
            }
        }
        
        gameTextMessageService.addGamMessages(msgDecision);
        
        Boolean isPlayerStuck = game.getIsPlayerStuckList().get(playerIndex);
        switchPlayer(game, playerIndex, isPlayerStuck);
        
        // after rolling die check if any three players are bankrupt or stuck at some property then game is over
        // and results will be displayed
        checkWin(game);
        propertyService.save(currProperty);
        gameRepository.save(game);


        return game;
    }

    public void moveToken(Game game) {

        // roll the die
        game.setDice1Value(getDiceFaceValue());
        game.setDice2Value(getDiceFaceValue());

        GameTextMessage msgSum = new GameTextMessage(null, game.getGameId(), game.getCurrentPlayer() + " has rolled a sum of  " + (game.getDice1Value() + game.getDice2Value()) + " on the die.");
        gameTextMessageService.addGamMessages(msgSum);

        GameTextMessage msgDec = new GameTextMessage(null, game.getGameId(), game.getCurrentPlayer() + " is taking the decision currently.");
        gameTextMessageService.addGamMessages(msgDec);

        String currentPlayerId = game.getCurrentPlayer();
        Token playerToken = game.getPlayerTokenMap().get(currentPlayerId);

        int diceTotal = game.getDice1Value() + game.getDice2Value();

        // Move the token according to the dice roll
        for (int i = 0; i < diceTotal; i++) {
            // Check if the token is at a corner
            if (isAtCorner(playerToken)) {
                int direction = playerToken.getDirection();
                if(direction == 1){
                    playerToken.setDirection(2);
                } else if(direction == 2){
                    playerToken.setDirection(3);
                }else if(direction == 3){
                    playerToken.setDirection(4);
                }else if(direction == 4){
                    playerToken.setDirection(1); 
                }
                moveTokenAtCorner(playerToken);
            }else if (isAtCornerMinusOne(playerToken)) {
                moveTokenAtCornerMinusOne(playerToken);
            }  else {
                moveTokenNormally(playerToken);
            }
        }
    }

    // Method to check if the token is at a corner
    private boolean isAtCorner(Token token) {
        int locX = token.getLocX();
        int locY = token.getLocY();
        return (locX == 0 && locY == 0) ||
               (locX == -10 && locY == 0) ||
               (locX == -10 && locY == -10) ||
               (locX == 0 && locY == -10);
    }

    private boolean isAtCornerMinusOne(Token token) {
        int locX = token.getLocX();
        int locY = token.getLocY();
        return (locX == 0 && locY == -1) ||
               (locX == -9 && locY == 0) ||
               (locX == -10 && locY == -9) ||
               (locX == -1 && locY == -10);
    }

    private void moveTokenAtCornerMinusOne(Token token) {
        int direction = token.getDirection();
        if (direction == 1) {
            token.setLocX(token.getLocX() - 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0) - inc1, token.getLocation().get(1)));
        } else if (direction == 2) {
            token.setLocY(token.getLocY() - 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0), token.getLocation().get(1) - inc2));
        } else if (direction == 3) {
            token.setLocX(token.getLocX() + 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0) + inc3, token.getLocation().get(1)));
        } else if (direction == 4) {
            token.setLocY(token.getLocY() + 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0), token.getLocation().get(1) + inc2));
        }
    }

    // Method to move the token when at a corner
    private void moveTokenAtCorner(Token token) {

        int direction = token.getDirection();
        if (direction == 1) {
            token.setLocX(token.getLocX() - 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0) - inc3, token.getLocation().get(1)));
        } else if (direction == 2) {
            token.setLocY(token.getLocY() - 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0), token.getLocation().get(1) - inc2));
        } else if (direction == 3) {
            token.setLocX(token.getLocX() + 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0) + inc1, token.getLocation().get(1)));
        } else if (direction == 4) {
            token.setLocY(token.getLocY() + 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0), token.getLocation().get(1) + inc2));
        }
    }

    // Method to move the token normally (not at a corner)
    private void moveTokenNormally(Token token) {
        int direction = token.getDirection();
        if (direction == 1) {
            token.setLocX(token.getLocX() - 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0) - inc1, token.getLocation().get(1)));
        } else if (direction == 2) {
            token.setLocY(token.getLocY() - 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0), token.getLocation().get(1) - inc1));
        } else if (direction == 3) {
            token.setLocX(token.getLocX() + 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0) + inc1, token.getLocation().get(1)));
        } else if (direction == 4) {
            token.setLocY(token.getLocY() + 1);
            token.setLocation(Arrays.asList(token.getLocation().get(0), token.getLocation().get(1) + inc1));
        }
    }

    private boolean checkWin(Game game) {
        int countStuck = 0;
        StringBuilder results = new StringBuilder("The Game is over.\n The results are as follows:\n");
        String winnerPlayerId = null;

        // Count the number of stuck players
        for (int i = 0; i < game.getIsPlayerStuckList().size(); i++) {
            boolean isStuck = game.getIsPlayerStuckList().get(i);
            if (isStuck) {
                countStuck++;
            } else {
                winnerPlayerId = game.getPlayerIdList().get(i);
            }
        }

        // If three players are stuck, the game is over
        if (countStuck == 3) {
            // Generate the results string for each player
            for (int i = 0; i < game.getPlayerIdList().size(); i++) {
                String playerId = game.getPlayerIdList().get(i);
                int playerMoney = game.getPlayerMoneyList().get(i);
                results.append(playerId).append(" : $").append(playerMoney).append("\n");
            }

            // Append the winner to the messages
            if (winnerPlayerId != null) {
                GameTextMessage msgWinner = new GameTextMessage(null, game.getGameId(), winnerPlayerId + " WINS the game.");
                gameTextMessageService.addGamMessages(msgWinner);
            }

            game.setStatus(winnerPlayerId + " WINS THE GAME");

            // Create the game over message
            GameTextMessage msgRes = new GameTextMessage(null, game.getGameId(), results.toString());
            gameTextMessageService.addGamMessages(msgRes);
            return true;
        }
        return false;
    } 


    public Game updateGameOnPlayerLeaving(String gameId, String leavingPlayerId){
        Game game = findGameById(gameId);

        gameTextMessageService.clearGameMessages();
        GameTextMessage msg1 = new GameTextMessage(null, game.getGameId(), leavingPlayerId + " has left the Game.");
        gameTextMessageService.addGamMessages(msg1);

        // game already won by user and he is trying to leave game, dont update anything
        if(game.getStatus().contains("WINS") || game.getStatus().contains("DRAW")) return null;
        
        if(game != null){
            // get leaving payer index from Id
            int leavingPlayerIndex = 0;
            for(String playerId : game.getPlayerIdList()){
                if(playerId.equals(leavingPlayerId)){
                    break;
                }
                leavingPlayerIndex++;
            }

            // check if game is not already won
            if(!checkWin(game)){
                    // remove player properties from propertyMap so others can buy or rent them
                    
                    // if last player remaining in game.playerStatusList then set status player wins
                    // else make playerstatus in playerstatus as false
                    int falseCount = 0;
                    for (Boolean status : game.getPlayerStatusList()) {
                        // Check if status is false i.e. player has left already
                        if (status != null && status == false) { 
                            falseCount++;
                        }
                    }
                    if(falseCount == 2){
                        // make status as false and make remainig player as winner
                        // now we got three players who left so we make remaining player as winner
                        game.getPlayerStatusList().set(leavingPlayerIndex, false);
                        int winnerIndex = 0;
                        for (Boolean status : game.getPlayerStatusList()) {
                            if (status == true) { 
                                break;
                            }
                            winnerIndex++;
                        }
                        String winningPlayer = game.getPlayerIdList().get(winnerIndex);
                        game.setStatus(winningPlayer + " WINS ");
                        
                        GameTextMessage msgWin = new GameTextMessage(null, game.getGameId(), game.getStatus().split(" ")[0] + " has won the Game.");
                        gameTextMessageService.addGamMessages(msgWin);
                    }else if(falseCount == 3){
                        // last player trying to leave
                        return null;
                    }else{
                        // else set status to false
                        game.getPlayerStatusList().set(leavingPlayerIndex, false);

                        // free up leaving players properties
                        game.getPropertyOwnerMap().get(leavingPlayerId).clear();

                        // If the leaving player is the current player, find the next active player
                        if (game.getCurrentPlayer().equals(leavingPlayerId)) {
                            int nextPlayerIndex = (leavingPlayerIndex + 1) % game.getPlayerIdList().size();
                            while (!game.getPlayerStatusList().get(nextPlayerIndex)) {
                                nextPlayerIndex = (nextPlayerIndex + 1) % game.getPlayerIdList().size();
                            }
                            game.setCurrentPlayer(game.getPlayerIdList().get(nextPlayerIndex));
                        }
                        GameTextMessage msgTurn = new GameTextMessage(null, game.getGameId(), game.getCurrentPlayer() + " has to roll the die.");
                        gameTextMessageService.addGamMessages(msgTurn);
                    }
                game = gameRepository.save(game);
            }else{
                // game is won and user is trying to leave now
                return null;
            }
        }
        return game;
    }

    private void switchPlayer(Game game, int playerIndex, Boolean isPlayerStuck) {
        System.out.println("switched player");
        // if player rolls doubles then he will get another roll of die
        if(game.getDice1Value() == game.getDice2Value() && !isPlayerStuck){

            // add to text area that now which player's turn it is
            GameTextMessage msg = new GameTextMessage(null, game.getGameId(), game.getCurrentPlayer() + " has rolled doubles.\n" + //
                    " He will roll die again.");
            gameTextMessageService.addGamMessages(msg);

        }else{
            // increase turn of player i.e. set turn to next player
            int nextPlayerIndex = (playerIndex + 1) % game.getPlayerIdList().size();
            while (!game.getPlayerStatusList().get(nextPlayerIndex)) {
                nextPlayerIndex = (nextPlayerIndex + 1) % game.getPlayerIdList().size();
            }
            game.setCurrentPlayer(game.getPlayerIdList().get(nextPlayerIndex));
        }

        GameTextMessage msgTurn = new GameTextMessage(null, game.getGameId(), game.getCurrentPlayer() + " has to roll the die.");
        gameTextMessageService.addGamMessages(msgTurn);
    }

    public Game sellProperty(SellPropertyDTO sellPropertyDTO){
        Game game = findGameById(sellPropertyDTO.getGameId());
        Property property = propertyService.getPropertyByName(sellPropertyDTO.getPropertyName());

        // get player index
        int playerIndex = game.getPlayerIdList().indexOf(sellPropertyDTO.getPlayerId());

        // remove from player propertyOwnerMap
        game.getPropertyOwnerMap().get(sellPropertyDTO.getPlayerId()).remove(property.getName());

        // increment player Money
        int playerMoney = game.getPlayerMoneyList().get(playerIndex);
        game.getPlayerMoneyList().set(playerIndex, playerMoney + property.getCost());

        // send msg
        gameTextMessageService.clearGameMessages();
        GameTextMessage msg = new GameTextMessage(null, game.getGameId(), sellPropertyDTO.getPlayerId() + " has sold " + sellPropertyDTO.getPropertyName() + " for $" + property.getCost() + ".");
        gameTextMessageService.addGamMessages(msg);

        gameRepository.save(game);
        return game;
    }
    
    private int getDiceFaceValue(){
        Random rand = new Random();
        return rand.nextInt(6) + 1;
    }
}
