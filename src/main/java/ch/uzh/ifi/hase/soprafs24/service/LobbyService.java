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
        // Fetch the lobby, handling the case where it might not exist
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        // Check if the user trying to leave is the owner
        boolean isOwnerLeaving = lobby.getLobbyOwner().getUsername().equals(username);

        lobby.getPlayers().forEach(player -> player.setLobby(null));
        playerRepository.saveAll(lobby.getPlayers());

        // Remove the player from the lobby
        lobby.getPlayers().removeIf(player -> player.getUsername().equals(username));

        // If the owner is leaving, handle lobby ownership transfer or lobby deletion
        if (isOwnerLeaving) {
            if (lobby.getPlayers().isEmpty()) {
                // No players left, delete the lobby if owner leaves and no one else is there
                lobbyRepository.delete(lobby);
            } else {
                // Assign a new owner from remaining players and update the lobby
                lobby.setLobbyOwner(lobby.getPlayers().get(0));
                lobbyRepository.save(lobby);
            }
        } else {
            // Just save the updated list of players if a non-owner is leaving
            lobbyRepository.save(lobby);
        }

        return true;  // Return true as the operation is expected to always succeed if no exceptions were thrown
    }

    private boolean areAllPlayersReady(Lobby lobby) {
        return lobby.getPlayers().stream().allMatch(Player::getReady);
    }

}
