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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
/*
    @Test
    void calculateLeaderboard_ShouldCalculateAndSortScores() throws Exception {
        Long gameId = 1L;
        Game currentGame =new Game();
        Round currentRound = new Round();
        String jsonScores = "{\"cat1\": {\"user1\": {\"score\": 10}, \"user2\": {\"score\": 5}}, \"cat2\": {\"user1\": {\"score\": 20}, \"user2\": {\"score\": 15}}}";
        currentRound.setRoundPoints(jsonScores);
        when(roundRepository.findTopByGameIdOrderByIdDesc(gameId)).thenReturn(Optional.of(currentRound));

        TypeReference<Map<String, Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {};
        lenient().when(objectMapper.readValue(eq(jsonScores), eq(typeRef))).thenReturn(buildScores());

        Map<String, Integer> leaderboard = roundService.calculateLeaderboard(gameId);

        assertEquals(Map.of("user1", 60, "user2", 40), leaderboard);
        assertTrue(leaderboard.get("user1") > leaderboard.get("user2"));
    }

    private Map<String, Map<String, Map<String, Object>>> buildScores() {
        Map<String, Map<String, Map<String, Object>>> scores = new HashMap<>();
        Map<String, Object> scoreDetails = new HashMap<>();
        scoreDetails.put("score", 30);
        scores.put("user1", Map.of("cat1", scoreDetails, "cat2", scoreDetails));
        scores.put("user2", Map.of("cat1", Map.of("score", 20), "cat2", Map.of("score", 20)));
        return scores;
    }

    @Test
    void adjustScores_ShouldApplyAdjustmentsCorrectly() throws Exception {
        Long gameId = 1L;
        Round currentRound = new Round();
        String originalScores = "{\"cat1\": {\"user1\": {\"score\": 10}, \"user2\": {\"score\": 5}}}";
        currentRound.setRoundPoints(originalScores);
        when(roundRepository.findTopByGameIdOrderByIdDesc(gameId)).thenReturn(Optional.of(currentRound));

        HashMap<String, HashMap<String, HashMap<String, Object>>> adjustments = new HashMap<>();
        HashMap<String, Object> user1Adjustments = new HashMap<>();
        user1Adjustments.put("veto", false);
        user1Adjustments.put("bonus", true);
        adjustments.put("cat1", new HashMap<>() {{
            put("user1", user1Adjustments);
        }});

        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(buildInitialScores());
        when(objectMapper.writeValueAsString(any())).thenReturn("updatedScores");

        Map<String, Map<String, Map<String, Object>>> adjustedScores = roundService.adjustScores(gameId, adjustments);

        assertNotNull(adjustedScores);
        assertEquals(13, adjustedScores.get("cat1").get("user1").get("score")); // Initial 10 + bonus 3
    }

    private Map<String, Map<String, Map<String, Object>>> buildInitialScores() {
        Map<String, Map<String, Map<String, Object>>> scores = new HashMap<>();
        Map<String, Object> user1Scores = new HashMap<>();
        user1Scores.put("score", 10);
        scores.put("cat1", new HashMap<>() {{
            put("user1", user1Scores);
        }});
        return scores;
    }



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
