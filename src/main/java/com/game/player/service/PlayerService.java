package com.game.player.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.monopoly.model.Game;
import com.game.monopoly.service.GameService;
import com.game.player.model.Player;
import com.game.player.model.PlayerStatsDTO;
import com.game.player.model.Status;
import com.game.player.repository.PlayerRepository;
import com.game.player.util.PasswordUtil;


@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameService gameService;

    public Player authenticateOrRegisterUser(Player user) {
        Optional<Player> existingUserOpt = playerRepository.findByNickName(user.getNickName());

        if (existingUserOpt.isPresent()) {
            Player existingUser = existingUserOpt.get();
            if (PasswordUtil.verifyPassword(user.getHashedPassword(), existingUser.getSalt(), existingUser.getHashedPassword())) {
                existingUser.setStatus(Status.ONLINE);
                existingUser.setLastOnline(new Date());
                playerRepository.save(existingUser);
                System.out.println("user is present and logged in successfully");
                return existingUser;
            } else {
                throw new RuntimeException("Password does not match");
            }
        } else {
            // Register a new user
            String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hashPassword(user.getHashedPassword(), salt);
            user.setSalt(salt);
            user.setHashedPassword(hashedPassword);
            user.setStatus(Status.ONLINE);
            user.setJoinedOn(new Date());
            playerRepository.save(user);
            return user;
        }
    }

    public void disconnect(Player user){
        Optional<Player> storedUserOpt = playerRepository.findByNickName(user.getNickName());
        if (storedUserOpt.isPresent()) {
            Player storedUser = storedUserOpt.get();
            storedUser.setStatus(Status.OFFLINE);
            storedUser.setLastOnline(new Date());
            playerRepository.save(storedUser);
            System.out.println("user set to offline");
            System.out.println("after setting to offline" + user);
        }
    }

    public List<Player> findConnectedUsers(){
        return playerRepository.findAllByStatus(Status.ONLINE);
    }

    public List<String> getAllOnlinePlayerIds(){
        return playerRepository.findAllNickNamesByStatusConnected();
    }

    public List<PlayerStatsDTO> findAllTopUsers(){
        List<PlayerStatsDTO> topUsers = new ArrayList<>();

        // get all users
        List<Player> users = playerRepository.findAll();

        // get each user stats and add to userstats
        for(Player user : users){
            topUsers.add(getPlayerStats(user.getNickName()));
        }

        // Sort the list based on wins in descending order
        Collections.sort(topUsers, new Comparator<PlayerStatsDTO>() {
            @Override
            public int compare(PlayerStatsDTO u1, PlayerStatsDTO u2) {
                int winComparison = Integer.compare(u2.getWins(), u1.getWins());
                if (winComparison != 0) {
                    return winComparison;
                }

                // If wins are equal, compare the ratio of games played to losses
                double ratio1 = (u1.getLosses() == 0) ? Double.POSITIVE_INFINITY : (double) u1.getGamesPlayed() / u1.getLosses();
                double ratio2 = (u2.getLosses() == 0) ? Double.POSITIVE_INFINITY : (double) u2.getGamesPlayed() / u2.getLosses();
                
                return Double.compare(ratio2, ratio1); // Higher ratio should come first
            }
        });

        return topUsers;
    }

    public PlayerStatsDTO getPlayerStats(String playerId){
        PlayerStatsDTO playerStatsDTO = new PlayerStatsDTO();
        List<Game> games = gameService.findAllPlayerGames(playerId);
        Optional<Player> userOpt = playerRepository.findByNickName(playerId);
        if(userOpt.isPresent()){
            Player user = userOpt.get();
            playerStatsDTO.setNickname(playerId);
            playerStatsDTO.setFullName(user.getFullName());
            playerStatsDTO.setStatus(String.valueOf(user.getStatus()));
            playerStatsDTO.setJoinedOn(user.getJoinedOn());
            playerStatsDTO.setLastOnline(user.getStatus() == Status.ONLINE ? new Date() : user.getLastOnline());

            // add played, wins, losses, draw
            int gamesPlayed = 0;
            int wins = 0;
            int losses = 0;
            int draws = 0;
            
            if(games.size() > 0){
                gamesPlayed = games.size();
                for (Game game : games) {
                    String status = game.getStatus();
                    if (status.contains(user.getNickName() + " WINS")) {
                        wins++;
                    } else if ("DRAW".equals(status)) {
                        draws++;
                    } else {
                        losses++;
                    }
                }
            }
            
            playerStatsDTO.setGamesPlayed(gamesPlayed);
            playerStatsDTO.setLosses(losses);
            playerStatsDTO.setDraws(draws);
            playerStatsDTO.setWins(wins);
        }
        return playerStatsDTO;
    }
}
