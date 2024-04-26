package ch.uzh.ifi.hase.soprafs24.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    public void getGameByLobbyId_returnsGame_whenGameExists() {
        Long lobbyId = 1L;
        Game expectedGame = new Game();
        expectedGame.setId(lobbyId);

        when(gameRepository.findByLobbyId(lobbyId)).thenReturn(Optional.of(expectedGame));

        Game actualGame = gameService.getGameByLobbyId(lobbyId);

        assertNotNull(actualGame);
        assertEquals(expectedGame, actualGame);
    }

    @Test
    public void getGameByLobbyId_throwsException_whenGameNotFound() {
        Long lobbyId = 2L;
        when(gameRepository.findByLobbyId(lobbyId)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> {
            gameService.getGameByLobbyId(lobbyId);
        });
    }}
