package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Statistic;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.websocket.WebSocketService;
import org.apache.catalina.Store;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/lobby")
public class LobbyController {
    @Autowired
    private LobbyRepository lobbyRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private LobbyService lobbyService;
    @Autowired
    private GameRepository gameRepository;


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

        lobbyRepository.save(createdLobby); // Update the lobby with the game link
        Game game = new Game();
        game.setStatus(GameStatus.VOTE);
        game.setRoundCount(1); // Ensure this is never null, set a default or calculated value
        game.setLobby(createdLobby);
        createdLobby.setGame(game);

        gameRepository.save(game);
        //start Websocket
        WebSocketService.startWebSocket(createdLobby.getLobbyId());
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
        if (lobby.getLobbyStatus() != LobbyStatus.SETUP) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot join lobby as the game is not in SETUP mode.");
        }

        Optional<Player> optionalPlayer=playerRepository.findByUsername(username);
        if(optionalPlayer.isEmpty()){return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Join Lobby failed user not found");

        }
        Player player=optionalPlayer.get();
        player.setLobby(lobby);
        lobby.getPlayers().add(player);
        lobbyRepository.save(lobby);

        // Return 200 OK with joined lobby data
        return ResponseEntity.ok(lobby);
    }
    @GetMapping("/players/{LobbyId}") //accept body (lobbyName) return only player
    public ResponseEntity<List<Player>> getAllPlayers(@PathVariable Long LobbyId) {
        Optional<Lobby> optionalLobby = lobbyRepository.findById(LobbyId);
        if (optionalLobby.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Lobby lobby = optionalLobby.get();
        List<Player> lobbyPlayers = lobby.getPlayers();
        return ResponseEntity.ok(lobbyPlayers);
    }


    @PutMapping("/settings/{LobbyId}")
    @Transactional
    public ResponseEntity<Lobby> updateLobbySettings(@PathVariable Long LobbyId, @RequestBody LobbyPutDTO settings) {
        Optional<Lobby> optionalLobby = lobbyRepository.findById(LobbyId);
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
        Optional<Lobby> optionalLobby = lobbyRepository.findById(LobbyId);
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

    @PutMapping("/leave/{lobbyId}")
    public ResponseEntity<Object> leaveLobby(@PathVariable Long lobbyId, @RequestParam String username) {
        boolean leftSuccessfully = lobbyService.leaveLobby(lobbyId, username);
        if (leftSuccessfully) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Player is not in the lobby.");
        }
    }
}
