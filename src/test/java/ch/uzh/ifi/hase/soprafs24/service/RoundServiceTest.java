package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
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
    @Mock
    private ObjectMapper objectMapper;

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
    void whenStartNewRound_withValidLobbyIdAndNullGame_shouldCreateRound() {
        Lobby lobby = new Lobby();
        lobby.setLobbyId(1L);

        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(gameRepository.save(any(Game.class))).then(AdditionalAnswers.returnsFirstArg());
        when(roundRepository.save(any(Round.class))).thenReturn(new Round());

        assertDoesNotThrow(() -> roundService.startNewRound(1L));
        verify(roundRepository).save(any(Round.class));
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
        when(roundRepository.findTopByGameIdOrderByIdDesc(1L)).thenReturn(Optional.of(latestRound));
        Round result = roundService.getCurrentRound(1L);
        assertEquals(latestRound, result);
    }

    @Test
    void getCurrentRound_whenNoRoundsExist_shouldReturnNull() {
        when(roundRepository.findTopByGameIdOrderByIdDesc(1L)).thenReturn(Optional.empty());
        Round result = roundService.getCurrentRound(1L);
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
        when(roundRepository.findByGameId(1L)).thenReturn(Collections.emptyList());
        List<Round> result = roundService.getRoundByGameId(1L);
        assertTrue(result.isEmpty());
    }
    @Test
    void getCurrentRoundByGameId_withExistingGameId_shouldReturnCurrentRound() {
        Round currentRound = new Round();
        when(roundRepository.findTopByGameIdOrderByIdDesc(1L)).thenReturn(Optional.of(currentRound)); // Directly return a Round object
        Round result = roundService.getCurrentRoundByGameId(1L);
        assertEquals(currentRound, result);
    }

    @Test
    void getCurrentRoundByGameId_withInvalidGameId_shouldReturnNull() {
        when(roundRepository.findTopByGameIdOrderByIdDesc(999L)).thenReturn(Optional.empty());
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
    @Test
    void testCalculateScoresCategoryWithAutoCorrectDisabled() throws Exception {
        // Mocking the Round object and its dependencies
        Lobby lobby = new Lobby();
        lobby.setAutoCorrectMode(false); // Set autoCorrectMode to false
        Game game = new Game();
        game.setLobby(lobby);
        Round currentRound = new Round();
        currentRound.setAssignedLetter('A'); // Set the assigned letter
        // Multiple scenarios in player answers
        currentRound.setPlayerAnswers("[{\"username\": \"user1\", \"category1\": \"apple\"}, {\"username\": \"user2\", \"category1\": \"avocado\"}, {\"username\": \"user3\", \"category1\": \"Axe\"}, {\"username\": \"user4\", \"category1\": \"axe\"}, {\"username\": \"user5\", \"category1\": \"banana\"}]");
        currentRound.setGame(game);

        // Mocking findTopByGameIdOrderByIdDesc
        when(roundRepository.findTopByGameIdOrderByIdDesc(1L)).thenReturn(Optional.of(currentRound));

        //Mocking objectMapper.readValue to accommodate the extended player answers
        when(objectMapper.readValue(Mockito.anyString(), Mockito.<TypeReference<List<Map<String, String>>>>any()))
                .thenReturn(Arrays.asList(
                        Map.of("username", "user1", "category1", "apple"),
                        Map.of("username", "user2", "category1", "avocado"),
                        Map.of("username", "user3", "category1", "Axe"),
                        Map.of("username", "user4", "category1", "axe"),
                        Map.of("username", "user5", "category1", "banana")
                ));

        // Call the method under test
        Map<String, Map<String, Map<String, Object>>> result = roundService.calculateScoresCategory(1L);

        // Assert the result
        Map<String, Map<String, Map<String, Object>>> expectedScores = new HashMap<>();
        Map<String, Map<String, Object>> userScoresAndAnswers = new HashMap<>();
        Map<String, Object> user1ScoreAndAnswer = new HashMap<>();
        user1ScoreAndAnswer.put("score", 10);
        user1ScoreAndAnswer.put("answer", "apple");
        Map<String, Object> user2ScoreAndAnswer = new HashMap<>();
        user2ScoreAndAnswer.put("score", 10);
        user2ScoreAndAnswer.put("answer", "avocado");
        Map<String, Object> user3ScoreAndAnswer = new HashMap<>();
        user3ScoreAndAnswer.put("score", 5);  // Score reduced due to duplication
        user3ScoreAndAnswer.put("answer", "Axe");
        Map<String, Object> user4ScoreAndAnswer = new HashMap<>();
        user4ScoreAndAnswer.put("score", 5);  // Score reduced due to duplication
        user4ScoreAndAnswer.put("answer", "axe");
        Map<String, Object> user5ScoreAndAnswer = new HashMap<>();
        user5ScoreAndAnswer.put("score", 0);  // Wrong starting letter
        user5ScoreAndAnswer.put("answer", "banana");
        userScoresAndAnswers.put("user1", user1ScoreAndAnswer);
        userScoresAndAnswers.put("user2", user2ScoreAndAnswer);
        userScoresAndAnswers.put("user3", user3ScoreAndAnswer);
        userScoresAndAnswers.put("user4", user4ScoreAndAnswer);
        userScoresAndAnswers.put("user5", user5ScoreAndAnswer);
        expectedScores.put("category1", userScoresAndAnswers);

        // Assert the result matches the expected scores
        assertEquals(expectedScores, result);
    }
    @Test
    void testAdjustScores_NoCurrentRound_ThrowsException() {
        Long gameId = 1L;
        when(roundRepository.findTopByGameIdOrderByIdDesc(gameId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> roundService.adjustScores(gameId, new HashMap<>()));
    }

}
