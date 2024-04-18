package ch.uzh.ifi.hase.soprafs24.service; // Correct package statement

import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
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
import java.util.Optional;


@Service
public class LobbyService {

    @Autowired
    private LobbyRepository lobbyRepository; // Corrected to instance call

    @Autowired
    private GameRepository gameRepository; // Corrected to instance call
    @Autowired
    private PlayerRepository playerRepository; // Added this field


    @Transactional
    public Lobby createLobby(String lobbyName, String lobbyPassword, Player owner) {
        if (lobbyName == null || lobbyName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby name must not be null or empty");
        }
        if (lobbyRepository.findByLobbyName(lobbyName).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lobby name must be unique");
        }
        Lobby lobby = new Lobby();
        lobby.setLobbyName(lobbyName);
        lobby.setLobbyPassword(lobbyPassword);
        lobby.setLobbyOwner(owner); // Set the owner's username

        // Set default values for other properties
        lobby.setRoundDuration(60); // Default round duration of 60 seconds
        lobby.setAutoCorrectMode(true);
        lobby.setCategories(new ArrayList<>()); // Empty list of categories by default
        lobby.setExcludedChars(new ArrayList<>()); // Empty list of excluded characters by default
        lobby.setGameMode("1");
        lobby.setLobbyStatus(LobbyStatus.SETUP);
        return lobbyRepository.save(lobby);


    }

    @Transactional
    public void deleteLobbyById(long lobbyId) {
        // Find the lobby by its ID
        Optional<Lobby> optionalLobby = lobbyRepository.findById(lobbyId);
        if (optionalLobby.isPresent()) {
            Lobby lobby = optionalLobby.get();

            // Delete the lobby from the repository
            lobbyRepository.delete(lobby);
        } else {
            // Lobby not found, throw an exception or handle the situation accordingly
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
        }
    }


    @Transactional
    public boolean leaveLobby(Long lobbyId, String username) {
        // Find the lobby by its ID
        Optional<Lobby> optionalLobby = lobbyRepository.findById(lobbyId);
        if (optionalLobby.isPresent()) {
            Lobby lobby = optionalLobby.get();

            // Check if player is in the lobby
            boolean playerInLobby = lobby.getPlayers().stream().anyMatch(player -> player.getUsername().equals(username));
            if (!playerInLobby) {
                return false; // Player is not in the lobby
            }

            // Check if the player is the owner
            if (lobby.getLobbyOwner().getUsername().equals(username)) {
                // If the player is the owner, find the next player to be assigned as owner
                Player nextOwner = null;
                if (lobby.getPlayers().size() > 1) {
                    nextOwner = lobby.getPlayers().get(1); // Assuming the second player becomes the owner
                }

                // If the next owner exists, assign them as the owner
                if (nextOwner != null) {
                    lobby.setLobbyOwner(nextOwner);
                } else {
                    // If the next owner doesn't exist (current owner is the last player), delete the lobby
                    deleteLobbyById(lobbyId);
                    return true; // Player successfully left the lobby
                }
            }

            // Remove the player from the lobby and update the lobby
            lobby.getPlayers().removeIf(player -> player.getUsername().equals(username));
            lobbyRepository.save(lobby);
            return true; // Player successfully left the lobby
        } else {
            // Lobby not found, throw an exception or handle the situation accordingly
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
        }
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
