package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private LobbyService lobbyService;

    @PostMapping("/create")
    public ResponseEntity<LobbyGetDTO> createLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        // Check if required fields are present
        if (lobbyPostDTO.getLobbyName() == null || lobbyPostDTO.getLobbyName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby name must not be empty");
        }

        // Create Lobby entity from DTO
        Lobby lobbyInput = DTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);

        // Perform any additional logic if needed, such as setting default values or validating data

        // Call service method to create lobby
        Lobby createdLobby = lobbyService.createLobby(lobbyInput.getLobbyName(), lobbyInput.getLobbyPassword());

        // Convert created lobby entity to DTO and return it
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(createdLobby));
    }

    @PostMapping("/join")
    public ResponseEntity<Object> joinLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        // Extract lobby name, lobby password, and username from the DTO
        String lobbyName = lobbyPostDTO.getLobbyName();
        String lobbyPassword = lobbyPostDTO.getLobbyPassword();
        String username = lobbyPostDTO.getUsername();
        String password = lobbyPostDTO.getPassword();

        // Check if lobby exists
        Optional<Lobby> optionalLobby = lobbyRepository.findByLobbyName(lobbyName);
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

        // Check if username is unique within the lobby


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

    @PutMapping("/settings")
    public ResponseEntity<Object> updateLobbySettings(@RequestParam String lobbyName, @RequestBody String settings) {
        // Implementiere die Logik zum Aktualisieren der Einstellungen einer bestimmten Lobby
        // Verwende lobbyName, um die Lobby zu identifizieren, und settings für die neuen Einstellungen
        // Rückgabe der entsprechenden Antwort, z.B. 204 NO CONTENT oder 400 BAD REQUEST
    }

    @GetMapping("/settings")
    public ResponseEntity<Object> getLobbySettings(@RequestParam String lobbyName) {
        // Implementiere die Logik zum Abrufen der Einstellungen einer bestimmten Lobby
        // Verwende lobbyName, um die Lobby zu identifizieren
        // Rückgabe der entsprechenden Antwort mit den Einstellungen oder einer Fehlermeldung
    }

    @PostMapping("/leave")
    public ResponseEntity<Object> leaveLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        // Implementiere die Logik zum Verlassen einer Lobby
        // Verwende lobbyLeaveDTO, um die erforderlichen Daten zu erhalten
        // Rückgabe der entsprechenden Antwort, z.B. 204 NO CONTENT oder 400 BAD REQUEST
    }

    @PostMapping("/delete")
    public ResponseEntity<Object> deleteLobby(@RequestBody String lobbyName) {
        // Implementiere die Logik zum Löschen einer Lobby
        // Verwende lobbyName, um die zu löschende Lobby zu identifizieren
        // Rückgabe der entsprechenden Antwort, z.B. 204 NO CONTENT oder 404 NOT FOUND
    } */
}
