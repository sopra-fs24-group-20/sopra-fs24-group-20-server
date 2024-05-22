package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class GameController {

    @Autowired
    private GameService gameService;
    @Autowired
    private GameRepository gameRepository;

    @GetMapping("/game/{LobbyId}") //accept body (lobbyName) return only gameId
    public ResponseEntity<Long> getGameId(@PathVariable Long LobbyId) {
        Optional<Game> optionalGame = gameRepository.findByLobbyId(LobbyId);
        if (optionalGame.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Game game = optionalGame.get();
        Long gameId = game.getId();
        return ResponseEntity.ok(gameId);
    }


    @PostMapping("/game/done/{lobbyId}")
    public ResponseEntity<String> finishGame(@PathVariable Long lobbyId) {
        try {
            Game game = gameService.getGameByLobbyId(lobbyId);
            game.setStatus(GameStatus.FINISHED);
            gameRepository.save(game);
            return ResponseEntity.ok("Game status updated to FINISHED");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update game status: " + e.getMessage());
        }
    }

}
