package com.game.monopoly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.game.monopoly.model.Property;

@Controller
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @GetMapping("/property")
    public ResponseEntity<Property> getProperty(@RequestParam int x, @RequestParam int y){
        return ResponseEntity.ok(propertyService.getPropertyByCoordinates(x, y));
    }
}
