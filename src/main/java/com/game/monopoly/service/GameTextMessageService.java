package com.game.monopoly.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.game.monopoly.model.GameTextMessage;
import com.game.monopoly.repository.GameTextMessageRepository;

@Service
public class GameTextMessageService {
    @Autowired
    private GameTextMessageRepository gameTextMessageRepository;

    private List<GameTextMessage> gameMsgs;    

    public GameTextMessageService(){
        gameMsgs = new ArrayList<GameTextMessage>();
    }

    public List<GameTextMessage> getGameMessages(){
        return gameMsgs;
    }

    public void setGamMessages(List<GameTextMessage> gameMsgs){
        this.gameMsgs = gameMsgs;
    }

    public void addGamMessages(GameTextMessage gameMsg){
        gameMsgs.add(gameMsg);
    }

    public void clearGameMessages(){
        gameMsgs.clear();
    }

    public List<GameTextMessage> getTextMessagesForGame(String gameId){
        return gameTextMessageRepository.findByGameId(gameId);
    }

    public List<GameTextMessage> saveMsgs(List<GameTextMessage> msgs){
        return gameTextMessageRepository.saveAll(msgs);
    }
}
