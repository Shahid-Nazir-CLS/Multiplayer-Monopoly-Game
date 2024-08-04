package com.game.player.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.game.monopoly.dto.GameDTO;
import com.game.monopoly.model.Game;
import com.game.monopoly.model.GameTextMessage;
import com.game.monopoly.service.GameService;
import com.game.monopoly.service.GameTextMessageService;
import com.game.player.model.Player;
import com.game.player.model.PlayerStatsDTO;
import com.game.player.service.MatchMakingService;
import com.game.player.service.PlayerService;


@Controller
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MatchMakingService matchMakingService;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameTextMessageService gameTextMessageService;

    /**
     * Handles adding a new user through WebSocket.
     * This method is triggered when a message is sent to /user/addUser.
     * The response is broadcasted to the public topic.
     *
     * @param user the user to be added
     * @return the added user
     */
    @MessageMapping("user.addUser")
    @SendTo("/topic/public")
    public Player addUser(@Payload Player user) { 
        return user;
    }

    /**
     * Handles user login via HTTP POST.
     * Authenticates or registers the user and returns the user details.
     *
     * @param user the user details for login
     * @return the authenticated or registered user
     */
    @PostMapping("/user/login")
    public ResponseEntity<Player> loginUser(@RequestBody Player user) {
        try {
            return ResponseEntity.ok(playerService.authenticateOrRegisterUser(user));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Handles user disconnection through WebSocket.
     * This method is triggered when a message is sent to /user/disconnectUser.
     * The response is broadcasted to the public topic.
     *
     * @param user the user to be disconnected
     * @return the disconnected user
     */
    @MessageMapping("user.disconnectUser")
    @SendTo("/topic/public")
    public Player disconnectUser(@Payload Player user) {
        System.out.println("User logged out");
        playerService.disconnect(user);
        return user;
    }

    /**
     * Handles user matchmaking through WebSocket.
     * This method is triggered when a message is sent to /user/matchUser.
     * If a match is found, it notifies both users and creates a new game.
     *
     * @param user the user requesting a match
     * @return the user
     */
    @MessageMapping("user.matchUser")
    public ResponseEntity<List<Player>> matchUser(@Payload Player player) {
        List<Player> players = matchMakingService.matchPlayer(player);

        if (players != null) {
            // Create a new game
            Game game = gameService.createNewGame(players);
            List<GameTextMessage> savedMessages = gameTextMessageService.saveMsgs(gameTextMessageService.getGameMessages());

            // Create game messages for each player
            for (Player pl : players) {
                // send game to all players
                messagingTemplate.convertAndSendToUser(pl.getNickName(), "/queue/gamestart", game);

                // send game messages to each player
                messagingTemplate.convertAndSendToUser(pl.getNickName(), "/queue/gamemessages", savedMessages);

            }
            gameTextMessageService.clearGameMessages();
        }
        return ResponseEntity.ok(players);
    }

    /**
     * Handles user leaving the game through WebSocket.
     * This method is triggered when a message is sent to /user/leaveGame.
     * It updates the game state and notifies the opponent if applicable.
     *
     * @param gameDto the game details
     */
    @MessageMapping("user.leaveGame")
    public void leaveGame(@Payload GameDTO gameDto) {
        System.out.println("User cancelled game: " + gameDto);
        
        Game game = gameService.updateGameOnPlayerLeaving(gameDto.getGameId(), gameDto.getPlayerId());
        List<GameTextMessage> savedMessages = gameTextMessageService.saveMsgs(gameTextMessageService.getGameMessages());

        if (game != null) {
             for(String player : game.getPlayerIdList()){
                messagingTemplate.convertAndSendToUser(player, "/queue/game", game);

                // send to players the game messages
                messagingTemplate.convertAndSendToUser(player, "/queue/gamemessages", savedMessages);
            }
            gameTextMessageService.clearGameMessages();
        }
    }

    /**
     * Retrieves a list of connected users.
     *
     * @return a list of connected users
     */
    @GetMapping("/user/connected")
    public ResponseEntity<List<Player>> findConnectedUsers() {
        return ResponseEntity.ok(playerService.findConnectedUsers());
    }

    /**
     * Retrieves statistics for a specific user.
     *
     * @param userId the ID of the user
     * @return the user's statistics
     */
    @GetMapping("/player/{userId}/stats")
    public ResponseEntity<PlayerStatsDTO> getPlayerStats(@PathVariable String userId) {
        return ResponseEntity.ok(playerService.getPlayerStats(userId));
    }

    /**
     * Retrieves the leaderboard of top users.
     *
     * @return a list of top users based on statistics
     */
    @GetMapping("/user/leaderboard")
    public ResponseEntity<List<PlayerStatsDTO>> getLeaderBoard() {
        return ResponseEntity.ok(playerService.findAllTopUsers());
    }
}