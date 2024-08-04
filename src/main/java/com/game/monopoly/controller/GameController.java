package com.game.monopoly.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.game.monopoly.dto.GameDTO;
import com.game.monopoly.dto.PlayerDecisionDTO;
import com.game.monopoly.dto.SellPropertyDTO;
import com.game.monopoly.model.Game;
import com.game.monopoly.model.GameTextMessage;
import com.game.monopoly.model.Property;
import com.game.monopoly.service.GameService;
import com.game.monopoly.service.GameTextMessageService;
import com.game.monopoly.service.PropertyService;
import com.game.player.model.Player;
import com.game.player.service.MatchMakingService;

@Controller
public class GameController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private GameService gameService;

    @Autowired
    private MatchMakingService matchMakingService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameTextMessageService gameTextMessageService;

    @GetMapping("/")
    public String getHome(){
        // gameService.createDummyGameData();
        return "home";
    }

    @GetMapping("/game")
    public String getBoard(){
        return "game";
    }
    
    // Get the details of a game by gameId
    @GetMapping("/game/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable String gameId) {
        return ResponseEntity.ok(gameService.findGameById(gameId));
    }

    // Get the game history for a player by playerId
    @GetMapping("/game/history/{playerId}")
    public ResponseEntity<List<Game>> getGames(@PathVariable String playerId) {
        return ResponseEntity.ok(gameService.findAllPlayerGames(playerId));
    }

    // Cancel a match for a player by removing them from the matchmaking queue
    @GetMapping("/game/cancel/{playerId}")
    public void cancelMatch(@PathVariable String playerId) {
        Player player = matchMakingService.removePlayerFromMatchQueue(playerId);
    }

    // Get all text messages for a specific game by gameId
    @GetMapping("/gamemessages/{gameId}")
    public ResponseEntity<List<GameTextMessage>> findGameMessages(@PathVariable("gameId") String gameId) {
        return ResponseEntity.ok(gameTextMessageService.getTextMessagesForGame(gameId));
    }

    // Handle the action of throwing a die in the game
    @MessageMapping("/game.throwDie")
    public void throwDie(@Payload GameDTO gameDto) {
        Game game = gameService.makeMove(gameDto.getGameId(), gameDto.getPlayerId());
        List<GameTextMessage> savedMessages = gameTextMessageService.saveMsgs(gameTextMessageService.getGameMessages());

        // Send the updated game state and messages to all players
        for (String playerId : game.getPlayerIdList()) {
            messagingTemplate.convertAndSendToUser(playerId, "/queue/game", game);
            messagingTemplate.convertAndSendToUser(playerId, "/queue/gamemessages", savedMessages);
        }

        // Clear messages after sending them to players
        gameTextMessageService.clearGameMessages();
    }

    // Handle the action of a player taking a decision in the game
    @MessageMapping("/game.takeDecision")
    public void takeDecision(@Payload PlayerDecisionDTO playerDecisionDTO) {
        System.out.println(playerDecisionDTO);
        Game game = gameService.takeDecision(playerDecisionDTO);
        List<GameTextMessage> savedMessages = gameTextMessageService.saveMsgs(gameTextMessageService.getGameMessages());

        // Send the updated game state and messages to all players
        for (String playerId : game.getPlayerIdList()) {
            messagingTemplate.convertAndSendToUser(playerId, "/queue/game", game);
            messagingTemplate.convertAndSendToUser(playerId, "/queue/gamemessages", savedMessages);
        }

        // Clear messages after sending them to players
        gameTextMessageService.clearGameMessages();
    }

    // Handle the action of a player selling a property in the game
    @MessageMapping("/game.sellProperty")
    public void sellProperty(@Payload SellPropertyDTO sellPropertyDTO) {
        System.out.println(sellPropertyDTO);
        Game game = gameService.sellProperty(sellPropertyDTO);
        List<GameTextMessage> savedMessages = gameTextMessageService.saveMsgs(gameTextMessageService.getGameMessages());

        // Send the updated game state and messages to all players
        for (String playerId : game.getPlayerIdList()) {
            messagingTemplate.convertAndSendToUser(playerId, "/queue/game", game);
            messagingTemplate.convertAndSendToUser(playerId, "/queue/gamemessages", savedMessages);
        }

        // Clear messages after sending them to players
        gameTextMessageService.clearGameMessages();
    }
}
