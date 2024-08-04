package com.game.monopoly.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    private int direction = 1;
	private int locX = 0;
	private int locY = 0;
    private String tokenColor;
    private List<Integer> location;
}
