package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/lobby")
public class LobbyController {
    @Autowired
    private LobbyRepository lobbyRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private LobbyService lobbyService;

    @PostMapping("/create")
    public ResponseEntity<LobbyGetDTO> createLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        // Check if required fields are present
        if (lobbyPostDTO.getLobbyName() == null || lobbyPostDTO.getLobbyName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby name must not be empty");
        }

        // Create Lobby entity from DTO
        Lobby lobbyInput = DTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);

        Player ownerPlayer = playerRepository.findByUsername(lobbyPostDTO.getOwnerUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        // Call service method to create lobby
        Lobby createdLobby = lobbyService.createLobby(lobbyInput.getLobbyName(), lobbyInput.getLobbyPassword(), ownerPlayer);

        // Convert created lobby entity to DTO and return it
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(createdLobby));
    }

    @PostMapping("/join")
    public ResponseEntity<Object> joinLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        // Extract lobby name, lobby password, and username from the DTO
        String lobbyName = lobbyPostDTO.getLobbyName();
        String lobbyPassword = lobbyPostDTO.getLobbyPassword();
        long lobbyId = lobbyPostDTO.getLobbyId();
        String username = lobbyPostDTO.getUsername();
        String password = lobbyPostDTO.getPassword();
        // Check if lobby exists
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyId(lobbyId);
        if (optionalLobby.isEmpty()) {
            // Lobby not found, return 404 NOT FOUND
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Join lobby failed because the lobby doesn’t exist");
        }
        // Lobby found, check if password matches
        Lobby lobby = optionalLobby.get();
        if (!lobby.getLobbyPassword().equals(lobbyPassword)) {
            // Password doesn't match, return 400 BAD REQUEST
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Join lobby failed because password doesn’t match");
        }
        if (lobby.getLobbyStatus() != LobbyStatus.SETUP) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot join lobby as the game is not in SETUP mode.");
        }


        // Password matches, create player and add to lobby
        Player player = new Player();
        player.setUsername(username);
        player.setPassword(password);
        player.setLobby(lobby);
        // Add player to lobby
        lobby.getPlayers().add(player);
        // Save changes to the database
        lobbyRepository.save(lobby);

        // Return 200 OK with joined lobby data
        return ResponseEntity.ok(lobby);
    }
    @PutMapping("/settings/{LobbyId}")
    @Transactional
    public ResponseEntity<Lobby> updateLobbySettings(@PathVariable Long LobbyId, @RequestBody LobbyPutDTO settings) {
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyId(LobbyId);
        if (optionalLobby.isEmpty()) {
            return ResponseEntity.notFound().build(); // Returns a 404 Not Found
        }

        Lobby lobby = optionalLobby.get();

        // Update settings if not null
        if (settings.getRoundDuration() != null) {
            lobby.setRoundDuration(settings.getRoundDuration());
        }
        if (settings.getGameMode() != null) {
            lobby.setGameMode(settings.getGameMode());
        }
        if (settings.getCategories() != null) {
            lobby.setCategories(settings.getCategories());
        }
        if (settings.getRounds() != null) {
            lobby.setRounds(settings.getRounds());
        }
        if (settings.getExcludedChars() != null) {
            lobby.setExcludedChars(settings.getExcludedChars());
        }
        if (settings.getAutoCorrectMode() != null) {
            lobby.setAutoCorrectMode(settings.getAutoCorrectMode());
        }
        if (settings.getLobbyStatus() != null) {
            lobby.setLobbyStatus(settings.getLobbyStatus());
        }

        // Save the updated lobby
        lobbyRepository.save(lobby);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/settings/{LobbyId}")
    public ResponseEntity<LobbyGetDTO> getLobbySettings(@PathVariable Long LobbyId) {
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyId(LobbyId);
        if (optionalLobby.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Lobby lobby = optionalLobby.get();
        //lobby.setLobbyStatus(LobbyStatus.SETUP);///////////////////////////////
        // Convert the Lobby entity to LobbyGetDTO
        LobbyGetDTO lobbyGetDTO = new LobbyGetDTO();
        lobbyGetDTO.setLobbyName(lobby.getLobbyName());
        lobbyGetDTO.setRoundDuration(lobby.getRoundDuration());
        lobbyGetDTO.setCategories(lobby.getCategories());
        lobbyGetDTO.setRounds(lobby.getRounds());
        lobbyGetDTO.setGameMode(lobby.getGameMode());
        lobbyGetDTO.setAutoCorrectMode(lobby.getAutoCorrectMode());
        lobbyGetDTO.setLobbyStatus(lobby.getLobbyStatus());

        // Return the LobbyGetDTO with the settings
        return ResponseEntity.ok(lobbyGetDTO);
    }


   /*
    @GetMapping("/players")
    public ResponseEntity<Object> getPlayers(@RequestParam String lobbyName) {
        // Implementiere die Logik zum Abrufen der Spieler in einer bestimmten Lobby
        // Verwende lobbyName, um die Lobby zu identifizieren
        // Rückgabe der entsprechenden Antwort mit den Spielern oder einer Fehlermeldung
    }

    @GetMapping("/status")
    public ResponseEntity<Object> getLobbyStatus(@RequestParam String lobbyName) {
        // Implementiere die Logik zum Abrufen des Status einer bestimmten Lobby
        // Verwende lobbyName, um die Lobby zu identifizieren
        // Rückgabe der entsprechenden Antwort mit dem Status oder einer Fehlermeldung
    }

    @PutMapping("/status")
    public ResponseEntity<Object> updateLobbyStatus(@RequestParam String lobbyName, @RequestBody String lobbyStatus) {
        // Implementiere die Logik zum Aktualisieren des Status einer bestimmten Lobby
        // Verwende lobbyName, um die Lobby zu identifizieren, und lobbyStatus für den neuen Status
        // Rückgabe der entsprechenden Antwort, z.B. 204 NO CONTENT oder 400 BAD REQUEST
    }




    */


    @PostMapping("/leave")
    public ResponseEntity<Object> leaveLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        // Extract lobby ID and player's username from the DTO
        String lobbyName = lobbyPostDTO.getLobbyName();
        long lobbyId = lobbyPostDTO.getLobbyId();
        String username = lobbyPostDTO.getUsername();

        // Check if lobby exists
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyId(lobbyId);
        if (optionalLobby.isEmpty()) {
            // Lobby not found, return 404 NOT FOUND
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Leave lobby failed because the lobby doesn’t exist");
        }

        // Lobby found, retrieve lobby entity
        Lobby lobby = optionalLobby.get();

        // Check if the player is part of the lobby
        boolean playerFound = false;
        for (Player player : lobby.getPlayers()) {
            if (player.getUsername().equals(username)) {
                // Player found, remove from lobby
                lobby.getPlayers().remove(player);
                playerFound = true;
                break;
            }
        }

        // If player is not found in the lobby, return a meaningful error message
        if (!playerFound) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Leave lobby failed because the player is not part of the lobby");
        }

        // Save changes to the database
        lobbyRepository.save(lobby);

        // Return 204 NO CONTENT to indicate successful leaving of the lobby
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Object> deleteLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        // Extract lobby ID and player's username from the DTO
        long lobbyId = lobbyPostDTO.getLobbyId();
        String username = lobbyPostDTO.getUsername();

        // Check if lobby exists
        Optional<Lobby> optionalLobby = lobbyRepository.findById(lobbyId);
        if (optionalLobby.isEmpty()) {
            // Lobby not found, return 404 NOT FOUND
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Delete lobby failed because the lobby doesn’t exist");
        }

        // Lobby found, retrieve lobby entity
        Lobby lobby = optionalLobby.get();

        // Check if the player is the owner of the lobby
        if (!lobby.getLobbyOwner().getUsername().equals(username)) {
            // The player is not the owner, return an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Only the lobby owner can delete the lobby");
        }

        // Delete the lobby
        lobbyRepository.delete(lobby);

        // Return 204 NO CONTENT to indicate successful deletion of the lobby
        return ResponseEntity.noContent().build();
    }

}
