package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LobbyControllerTest {

    @Mock
    private LobbyRepository lobbyRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private LobbyService lobbyService;
    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private LobbyController lobbyController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    void createLobby_ShouldCreateLobbySuccessfully() {
        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("MyLobby");
        lobbyPostDTO.setOwnerUsername("ownerUser");
        lobbyPostDTO.setLobbyPassword("123");

        Player owner = new Player();
        owner.setUsername("ownerUser");

        Lobby lobby = new Lobby();
        lobby.setLobbyStatus(LobbyStatus.SETUP);
        lobby.setLobbyName("MyLobby");
        lobby.setLobbyOwner(owner);
        Game game = new Game();
        game.setStatus(GameStatus.VOTE);
        game.setRoundCount(1);
        lobby.setGame(game);

        when(playerRepository.findByUsername("ownerUser")).thenReturn(Optional.of(owner));
        when(lobbyService.createLobby(eq("MyLobby"), anyString(), eq(owner))).thenReturn(lobby);

        ResponseEntity<LobbyGetDTO> response = lobbyController.createLobby(lobbyPostDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MyLobby", response.getBody().getLobbyName());
    }

    @Test
    void joinLobby_ShouldReturnLobbyOnSuccessfulJoin() {
        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("MyLobby");
        lobbyPostDTO.setLobbyPassword("pass123");
        lobbyPostDTO.setUsername("joiningUser") ;

        Lobby lobby = new Lobby();
        lobby.setLobbyStatus(LobbyStatus.SETUP);
        lobby.setLobbyName("MyLobby");
        lobby.setLobbyPassword("pass123");

        Player player = new Player();
        player.setUsername("joiningUser");

        when(lobbyRepository.findByLobbyName("MyLobby")).thenReturn(Optional.of(lobby));
        when(playerRepository.findByUsername("joiningUser")).thenReturn(Optional.of(player));

        ResponseEntity<Object> response = lobbyController.joinLobby(lobbyPostDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Lobby);
    }
    @Test
    void updateLobbySettings_ShouldUpdateSettings() {
        Long lobbyId = 1L;
        LobbyPutDTO settings = new LobbyPutDTO();
        settings.setRoundDuration(90);

        Lobby lobby = new Lobby();
        lobby.setLobbyId(lobbyId);
        lobby.setRoundDuration(60); // Initial setting

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));

        ResponseEntity<Lobby> response = lobbyController.updateLobbySettings(lobbyId, settings);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(lobbyRepository).save(lobby);
        assertEquals(90, lobby.getRoundDuration());
    }
    @Test
    void leaveLobby_ShouldReturnOkOnSuccess() {
        Long lobbyId = 1L;
        String username = "userLeaving";

        when(lobbyService.leaveLobby(lobbyId, username)).thenReturn(true);

        ResponseEntity<Object> response = lobbyController.leaveLobby(lobbyId, username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void createLobby_ShouldHandleNullReturn() {
        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("MyLobby");
        lobbyPostDTO.setOwnerUsername("ownerUser");
        lobbyPostDTO.setLobbyPassword("123");

        when(playerRepository.findByUsername("ownerUser")).thenReturn(Optional.of(new Player()));
        when(lobbyService.createLobby(anyString(), anyString(), any(Player.class))).thenReturn(null);

        ResponseEntity<LobbyGetDTO> response = lobbyController.createLobby(lobbyPostDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testCreateLobby_WhenPlayerNotFound() {
        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyName("Test Lobby");
        lobbyPostDTO.setOwnerUsername("nonexistentUser");

        when(playerRepository.findByUsername("nonexistentUser")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            lobbyController.createLobby(lobbyPostDTO);
        });
    }

    @Test
    void getLobbySettings_ShouldReturnLobbySettingsWhenLobbyExists() {
        // Arrange
        Long lobbyId = 1L;
        Lobby lobby = new Lobby();
        lobby.setLobbyId(lobbyId);
        lobby.setLobbyName("Test Lobby");
        lobby.setRoundDuration(5);
        lobby.setCategories(Arrays.asList("category1", "category2"));
        lobby.setRounds(3);
        lobby.setGameMode("CLASSIC");
        lobby.setAutoCorrectMode(true);
        lobby.setLobbyStatus(LobbyStatus.SETUP);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));

        // Act
        ResponseEntity<LobbyGetDTO> response = lobbyController.getLobbySettings(lobbyId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Lobby", response.getBody().getLobbyName());
        assertEquals(Integer.valueOf(5), response.getBody().getRoundDuration());
        assertIterableEquals(Arrays.asList("category1", "category2"), response.getBody().getCategories());
        assertEquals(3, response.getBody().getRounds());
        assertEquals("CLASSIC", response.getBody().getGameMode());
        assertEquals(LobbyStatus.SETUP, response.getBody().getLobbyStatus());
    }

    @Test
    void getLobbySettings_ShouldReturnNotFoundWhenLobbyDoesNotExist() {
        // Arrange
        Long lobbyId = 1L;
        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<LobbyGetDTO> response = lobbyController.getLobbySettings(lobbyId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    @Test
    void getAllPlayers_WhenLobbyExists_ShouldReturnPlayers() {
        // Arrange
        Long lobbyId = 1L;
        Player player1 = new Player();
        player1.setUsername("player1");
        Player player2 = new Player();
        player2.setUsername("player2");
        List<Player> players = Arrays.asList(player1, player2);

        Lobby lobby = new Lobby();
        lobby.setLobbyId(lobbyId);
        lobby.setPlayers(players);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));

        // Act
        ResponseEntity<List<Player>> response = lobbyController.getAllPlayers(lobbyId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().containsAll(players));
    }

    @Test
    void getAllPlayers_WhenLobbyDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        Long lobbyId = 1L;
        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<Player>> response = lobbyController.getAllPlayers(lobbyId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

}
