package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameController gameController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getGameId_GameExists_ShouldReturnGameId() {
        Long lobbyId = 1L;
        Game game = new Game();
        game.setId(2L);

        when(gameRepository.findByLobbyId(lobbyId)).thenReturn(Optional.of(game));

        ResponseEntity<Long> response = gameController.getGameId(lobbyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody());
    }

    @Test
    void getGameId_GameDoesNotExist_ShouldReturnNotFound() {
        Long lobbyId = 1L;

        when(gameRepository.findByLobbyId(lobbyId)).thenReturn(Optional.empty());

        ResponseEntity<Long> response = gameController.getGameId(lobbyId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void finishGame_GameExistsAndUpdates_ShouldReturnSuccessMessage() {
        Long lobbyId = 1L;
        Game game = new Game();
        game.setId(lobbyId);

        when(gameService.getGameByLobbyId(lobbyId)).thenReturn(game);

        ResponseEntity<String> response = gameController.finishGame(lobbyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game status updated to FINISHED", response.getBody());
        assertEquals(GameStatus.FINISHED, game.getStatus());
    }

    @Test
    void finishGame_GameDoesNotExist_ShouldReturnBadRequest() {
        Long lobbyId = 1L;

        when(gameService.getGameByLobbyId(lobbyId)).thenThrow(new RuntimeException("Game not found"));

        ResponseEntity<String> response = gameController.finishGame(lobbyId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to update game status: Game not found"));
    }
}
