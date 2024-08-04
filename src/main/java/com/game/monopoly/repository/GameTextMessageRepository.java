package com.game.monopoly.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.game.monopoly.model.GameTextMessage;

@Repository
public interface GameTextMessageRepository extends MongoRepository<GameTextMessage, String>{
        List<GameTextMessage> findByGameId(String gameId);
}
