package com.game.monopoly.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "properties")
public class Property {
    @Id
    private String propertyId;
    private String name; 
    private int cost; 
    private int rent; 
    private int reward; 
    private String owner; 
    private int x; 
    private int y;
}
