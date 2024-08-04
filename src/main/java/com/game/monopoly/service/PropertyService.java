package com.game.monopoly.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.monopoly.model.Property;
import com.game.monopoly.repository.PropertyRepository;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    public Property getPropertyByCoordinates(int x, int y) {
        Optional<Property> property = propertyRepository.findByXAndY(x, y);
        return property.orElse(null); // Return the property if present, or null if not
    }

    public Property getPropertyByName(String name) {
        Optional<Property> property = propertyRepository.findByName(name);
        return property.orElse(null); // Return the property if present, or null if not
    }

    public void save(Property property){
        propertyRepository.save(property);
    }
}
