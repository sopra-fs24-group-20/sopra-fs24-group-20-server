package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static ch.uzh.ifi.hase.soprafs24.constant.GameStatus.ANSWER;

@Service
public class GameService {
    private final GameRepository gameRepository;
    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
    public Boolean checkAllReady(){
        return true;
    }
    public void startGame(){
        if(checkAllReady()){
            Game game = new Game();
            game.setStatus(ANSWER);
            //start round//////////////////////////////////////////////////////////////////////////////
        }

    }
}
