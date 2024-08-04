package com.game.monopoly.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.game.monopoly.model.Property;

@Repository
public interface PropertyRepository extends MongoRepository<Property, String>{
    Optional<Property> findByXAndY(int x, int y);
    Optional<Property> findByName(String name);
}
