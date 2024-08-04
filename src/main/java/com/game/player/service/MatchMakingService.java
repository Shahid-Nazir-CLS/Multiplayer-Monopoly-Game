package com.game.player.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.game.player.model.Player;

@Service
public class MatchMakingService {

    // Queue to hold players waiting for a match
    private Queue<Player> waitingUsers = new LinkedList<>();

    /**
     * Attempts to match a player with other players.
     * If there are less than four players in the queue, adds the player to the queue and returns null.
     * If there are four players, removes them from the queue and returns them as a group.
     *
     * @param user The player to be added to the match queue.
     * @return A list of four players if a group is formed, otherwise null.
     */
    public List<Player> matchPlayer(Player user) {
        // Add the user to the waiting queue
        waitingUsers.add(user);

        // If there are at least four players, form a group
        if (waitingUsers.size() >= 4) {
            List<Player> group = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                group.add(waitingUsers.poll());
            }
            System.out.println("got 4 players and hence returned match");
            return group;
        }

        // Not enough players to form a group yet
        System.out.println("added player to queue");
        return null;
    }

    /**
     * Removes a player from the match queue based on their nickname.
     *
     * @param userId The nickname of the player to remove.
     * @return The removed player if found, otherwise null.
     */
    public Player removePlayerFromMatchQueue(String userId){
        Iterator<Player> iterator = waitingUsers.iterator();

        // Iterate through the queue to find the player
        while (iterator.hasNext()) {
            Player user = iterator.next();
            if (user.getNickName().equals(userId)) {
                // Remove the player from the queue
                iterator.remove();
                System.out.println("Removed user from queue: " + userId);
                return user;
            }
        }

        // Player not found in the queue
        System.out.println("User not found in queue: " + userId);
        return null;
    }
}
