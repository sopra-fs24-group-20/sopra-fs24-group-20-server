package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

class RoundServiceTest {

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private LobbyRepository lobbyRepository;

    @InjectMocks
    private RoundService roundService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void whenStartNewRound_withValidLobbyId_shouldCreateRound() {
        Lobby lobby = new Lobby();
        lobby.setLobbyId(1L);
        Game game = new Game();
        lobby.setGame(game);

        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        when(roundRepository.save(any(Round.class))).thenReturn(new Round());

        assertDoesNotThrow(() -> roundService.startNewRound(1L));
        verify(roundRepository).save(any(Round.class));
    }

    @Test
    void whenStartNewRound_withInvalidLobbyId_shouldThrowException() {
        when(lobbyRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> roundService.startNewRound(999L));
    }

    @Test
    void getCurrentRoundLetter_withExistingGame_shouldReturnLetter() {
        Game game = new Game();
        Round round = new Round();
        round.setAssignedLetter('A');
        game.setRounds(List.of(round));

        when(gameRepository.findById(anyLong())).thenReturn(Optional.of(game));

        char result = roundService.getCurrentRoundLetter(1L);
        assertEquals('A', result);
    }

    @Test
    void getCurrentRoundLetter_withNoGameFound_shouldReturnNullChar() {
        when(gameRepository.findById(anyLong())).thenReturn(Optional.empty());

        char result = roundService.getCurrentRoundLetter(1L);
        assertEquals('\0', result);
    }

    // Add more tests for other methods such as calculateScoresCategory, getCurrentRoundByGameId, etc.
}
