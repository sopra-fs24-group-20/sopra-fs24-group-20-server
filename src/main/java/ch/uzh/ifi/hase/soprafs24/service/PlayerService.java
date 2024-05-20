package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public Player createPlayer(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must not be null or empty");
        }
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username must be unique");
        }
        Player player = new Player();
        player.setUsername(username);
        player.setPassword(password);
        player.setReady(false);
        player.setTotalPoints(0);
        player.setRoundsPlayed(0);
        player.setLevel(1);
        player.setAveragePointsPerRound(0);
        player.setVictories(0);
        player.setOnline(false);
        if (username.startsWith("Guest:")) {
            player.setOnline(true);
        }
        return playerRepository.save(player);
    }

    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }
    public Player updatePlayer(String username, Player updatedPlayer) {
        return playerRepository.findByUsername(username).map(player -> {
            // Update player's fields here. For example:
            if (updatedPlayer.getPassword() != null) {
                player.setPassword(updatedPlayer.getPassword());
            }
            player.setReady(updatedPlayer.getReady());
            // Add other fields you wish to update.
            // LobbyService.checkAndStartGame();
            return playerRepository.save(player);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found with username: " + username));
    }

    public Player getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found with username: " + username));
    }

    public Player LogInPlayer(String username, String password) {

        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Password Incorrect"));

        if (player.getOnline()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Player is already logged in");
        }

        if (!password.equals(player.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Password Incorrect");
        }

        player.setOnline(true);
        return playerRepository.save(player);
    }

    public Player LogOutPlayer(String username) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with username: " + username));


        if (username.startsWith("Guest:")) {
            // Delete the player from the database if the username starts with "Guest:"
            playerRepository.delete(player);
            return null;  // Return null to indicate that the player has been deleted
        } else {
            if (!player.getOnline()) {
                throw new IllegalStateException("Player is not currently logged in");
            }
            player.setOnline(false);
            return playerRepository.save(player);
        }
    }

    public int calculateLevel(int totalPoints) {
        int level = 1;
        while (25 * Math.pow(level, 2) <= totalPoints) {
            level++;
        }
        return level - 1;  // Subtract 1 because level increments once more after the last valid level
    }
}