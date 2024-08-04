package com.game.monopoly.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.game.monopoly.model.Game;

public interface GameRepository extends MongoRepository<Game, String>{
    // return games sorted in descending order of start time
    List<Game> findByPlayerIdList(String playerId, Sort sort);

}
