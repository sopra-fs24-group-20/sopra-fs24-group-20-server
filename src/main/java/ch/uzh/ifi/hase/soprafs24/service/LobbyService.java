package ch.uzh.ifi.hase.soprafs24.service; // Correct package statement

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;


@Service
public class LobbyService {

    @Autowired
    private LobbyRepository lobbyRepository; // Corrected to instance call
    @Autowired
    private GameRepository gameRepository; // Corrected to instance call
    @Autowired
    private PlayerRepository playerRepository; // Added this field


    public Lobby createLobby(String lobbyName, String lobbyPassword) {
        if (lobbyName == null || lobbyName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby name must not be null or empty");
        }
        if (lobbyRepository.findByLobbyName(lobbyName).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lobby name must be unique");
        }
        Lobby lobby = new Lobby();
        lobby.setLobbyName(lobbyName);
        lobby.setLobbyPassword(lobbyPassword);

        // Set default values for other properties
        lobby.setRoundDuration(60); // Default round duration of 60 seconds
        lobby.setAutoCorrectMode(true);
        lobby.setCategories(new ArrayList<>()); // Empty list of categories by default
        lobby.setExcludedChars(new ArrayList<>()); // Empty list of excluded characters by default
        lobby.setGameMode("1");
        // lobby.setGameStatus(GameStatus.SETUP);
        return lobbyRepository.save(lobby);
    }
    @Transactional
    public void checkAndStartGame(Lobby lobby) {
        if (areAllPlayersReady(lobby)) {
            Game game = lobby.getGame(); // This assumes the Lobby entity has a getGame() method
            if (game != null && game.getStatus() != GameStatus.ANSWER) {
                game.setStatus(GameStatus.ANSWER);
                gameRepository.save(game);
            }
        }
    }

    private boolean areAllPlayersReady(Lobby lobby) {
        return lobby.getPlayers().stream().allMatch(Player::getReady);
    }
}
