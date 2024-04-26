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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ExtendWith(MockitoExtension.class)
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
    @Test
    void getCurrentRound_shouldReturnLatestRound() {
        Round latestRound = new Round();
        when(roundRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(latestRound));
        Round result = roundService.getCurrentRound();
        assertEquals(latestRound, result);
    }

    @Test
    void getCurrentRound_whenNoRoundsExist_shouldReturnNull() {
        when(roundRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        Round result = roundService.getCurrentRound();
        assertNull(result);
    }
    @Test
    void getRoundByGameId_withExistingGameId_shouldReturnRounds() {
        List<Round> rounds = List.of(new Round(), new Round());
        when(roundRepository.findByGameId(1L)).thenReturn(rounds);
        List<Round> result = roundService.getRoundByGameId(1L);
        assertEquals(2, result.size());
        assertEquals(rounds, result);
    }

    @Test
    void getRoundByGameId_withNoRoundsFound_shouldReturnEmptyList() {
        when(roundRepository.findByGameId(999L)).thenReturn(Collections.emptyList());
        List<Round> result = roundService.getRoundByGameId(999L);
        assertTrue(result.isEmpty());
    }
    @Test
    void getCurrentRoundByGameId_withExistingGameId_shouldReturnCurrentRound() {
        Round currentRound = new Round();
        when(roundRepository.findTopByGameIdOrderByIdDesc(1L)).thenReturn(currentRound); // Directly return a Round object
        Round result = roundService.getCurrentRoundByGameId(1L);
        assertEquals(currentRound, result);
    }

    @Test
    void getCurrentRoundByGameId_withInvalidGameId_shouldReturnNull() {
        when(roundRepository.findTopByGameIdOrderByIdDesc(999L)).thenReturn(null); // Return null for an invalid ID
        Round result = roundService.getCurrentRoundByGameId(999L);
        assertNull(result);
    }

    @Test
    void saveRound_shouldSaveRound() {
        Round round = new Round();
        roundService.saveRound(round);
        verify(roundRepository).save(round);
    }


    @Test
    void calculateLeaderboard_withNoCurrentRound_shouldThrowException() {
        when(roundService.getCurrentRoundByGameId(999L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> roundService.calculateLeaderboard(999L));
    }
    /*
    @Test
    void calculateLeaderboard_withValidGameId_shouldReturnSortedScores() throws Exception {
        // Arrange
        Round currentRound = new Round();
        currentRound.setAssignedLetter('A');
        // Ensure the stub returns a Round object or an Optional of Round if that's the expected method signature.
        when(roundRepository.findTopByGameIdOrderByIdDesc(1L)).thenReturn(currentRound);

        // Assuming the calculateScoresCategory returns the following complex structure
        Map<String, Map<String, Map<String, Object>>> scoresByCategory = new HashMap<>();
        Map<String, Object> userDetails1 = new HashMap<>();
        userDetails1.put("score", 50);
        Map<String, Object> userDetails2 = new HashMap<>();
        userDetails2.put("score", 70);

        Map<String, Map<String, Object>> categoryScores = new HashMap<>();
        categoryScores.put("user1", userDetails1);
        categoryScores.put("user2", userDetails2);

        scoresByCategory.put("category1", categoryScores);

        // Stubbing calculateScoresCategory to return the complex map structure
        when(roundService.calculateScoresCategory(1L)).thenReturn(scoresByCategory);

        // Act
        Map<String, Integer> result = roundService.calculateLeaderboard(1L);

        // Assert
        assertEquals(2, result.size());
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(result.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        // Check for proper sorting
        assertEquals("user2", sortedEntries.get(0).getKey());
        assertEquals(Integer.valueOf(70), sortedEntries.get(0).getValue());
        assertEquals("user1", sortedEntries.get(1).getKey());
        assertEquals(Integer.valueOf(50), sortedEntries.get(1).getValue());
    }
*/

    // Add more tests for other methods such as calculateScoresCategory, getCurrentRoundByGameId, etc.
}