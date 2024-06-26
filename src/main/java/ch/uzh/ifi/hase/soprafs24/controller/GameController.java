package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.RoundService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

@Controller
public class GameController {

    @Autowired
    private GameService gameService;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private RoundService roundService;
    @Autowired
    private LobbyRepository lobbyRepository;

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
    public ResponseEntity<String> finishGame(@PathVariable Long lobbyId, @RequestBody String username) {
        try {
            Game game = gameService.getGameByLobbyId(lobbyId);
            game.setStatus(GameStatus.FINISHED);
            long gameId = game.getId();
            game.getLobby().setLobbyStatus(LobbyStatus.SETUP);
            gameRepository.save(game);
            lobbyRepository.save(game.getLobby());
            String existingGamePointsJson = game.getGamePoints();
            Map<String, Integer> gamePoints;
            String owner = game.getLobby().getLobbyOwner().getUsername();
            if (existingGamePointsJson != null && !existingGamePointsJson.isEmpty()) {
                gamePoints = objectMapper.readValue(existingGamePointsJson, new TypeReference<Map<String, Integer>>() {});
            } else {
                gamePoints = new HashMap<>();
            }
            if (!gamePoints.isEmpty()) {
                    roundService.updatePlayerStatsAndCheckVictories(gameId, gamePoints);
            }

            return ResponseEntity.ok("Game status updated to FINISHED");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update game status: " + e.getMessage());
        }
    }

}
