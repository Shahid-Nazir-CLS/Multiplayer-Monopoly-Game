package com.game.player.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.game.player.model.Player;
import com.game.player.model.Status;

@Repository
public interface PlayerRepository extends MongoRepository<Player, String>{
    Optional<Player> findByNickName(String nickName);
    List<Player> findAllByStatus(Status status);
    // Method to retrieve all player nicknames with status "CONNECTED"
    @Query(value = "{ 'status': 'ONLINE' }", fields = "{ 'nickName' : 1, '_id' : 0 }")
    List<String> findAllNickNamesByStatusConnected();
}
