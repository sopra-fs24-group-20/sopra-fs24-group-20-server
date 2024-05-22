package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
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
    private PlayerRepository playerRepository;

    @Mock
    private LobbyRepository lobbyRepository;

    @InjectMocks
    private RoundService roundService;
    @Mock
    private PlayerService playerService;

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

/*
    @Test
    void calculateLeaderboard_withNoCurrentRound_shouldThrowException() {
        when(roundService.getCurrentRoundByGameId(999L)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> roundService.calculateLeaderboard(999L));


    }*/

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
        user1ScoreAndAnswer.put("score", 1);
        user1ScoreAndAnswer.put("answer", "apple");
        Map<String, Object> user2ScoreAndAnswer = new HashMap<>();
        user2ScoreAndAnswer.put("score", 1);
        user2ScoreAndAnswer.put("answer", "avocado");
        Map<String, Object> user3ScoreAndAnswer = new HashMap<>();
        user3ScoreAndAnswer.put("score", 1);
        user3ScoreAndAnswer.put("answer", "Axe");
        Map<String, Object> user4ScoreAndAnswer = new HashMap<>();
        user4ScoreAndAnswer.put("score", 1);
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

        assertThrows(RuntimeException.class, () -> roundService.prepareScoreAdjustments(gameId, new HashMap<>()));
    }



    /*@Test
    public void testCalculateLeaderboard() throws Exception {
        // Setup
        Game game = new Game();
        Lobby lobby = new Lobby();
        Round round = new Round();
        lobby.setRounds(1);  // Assuming this is the final round
        game.setLobby(lobby);
        List<Round> rounds = new ArrayList<>();
        rounds.add(round);
        game.setRounds(rounds);
        round.setGame(game);

        Player player1 = new Player();
        player1.setUsername("player1");
        player1.setRoundsPlayed(0);
        player1.setLevel(1);
        player1.setTotalPoints(20);
        player1.setVictories(0);
        Player player2 = new Player();
        player2.setUsername("player2");
        player2.setRoundsPlayed(0);
        player2.setLevel(1);
        player2.setTotalPoints(0);
        player2.setVictories(0);

        Long gameId = 1L;

        round.setRoundPoints("{\"category1\":{\"player1\":{\"score\":100},\"player2\":{\"score\":150}}}");

        when(roundRepository.findTopByGameIdOrderByIdDesc(1L)).thenReturn(Optional.of(round));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(playerService.getPlayerByUsername("player1")).thenReturn(player1);
        when(playerService.getPlayerByUsername("player2")).thenReturn(player2);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Map.of(
                        "category1", Map.of(
                                "player1", Map.of("score", 100),
                                "player2", Map.of("score", 150)
                        )
                ));

        // Execute
        Map<String, Integer> leaderboard = roundService.calculateLeaderboard(gameId);

        // Verify
        assertEquals(2, leaderboard.size());
        assertTrue(leaderboard.containsKey("player1") && leaderboard.get("player1") == 100);
        assertTrue(leaderboard.containsKey("player2") && leaderboard.get("player2") == 150);
        assertEquals(Integer.valueOf(150), leaderboard.get("player2")); // Player 2 should be the highest scorer


        assertEquals(120, player1.getTotalPoints());
        assertEquals(1, player2.getRoundsPlayed());
        // Verify final round victory increment
        assertEquals(1, player2.getVictories());
        assertEquals(0, player1.getVictories());
    }*/

    /*
    @Test
    public void testAreAllVotesSubmitted() {
        // Setup
        Long gameId = 1L;
        Round currentRound = new Round();
        Lobby lobby = new Lobby();
        List<Player> players = List.of(new Player(), new Player(), new Player()); // Assume three players
        lobby.setPlayers(players);
        currentRound.setGame(new Game());
        currentRound.getGame().setLobby(lobby);
        String roundPointsJson = "{\"category1\": {\"player1\": {\"submissionsCount\": 3}, \"player2\": {\"submissionsCount\": 3}, \"player3\": {\"submissionsCount\": 3}}, \"category2\": {\"player1\": {\"submissionsCount\": 3}, \"player2\": {\"submissionsCount\": 3}, \"player3\": {\"submissionsCount\": 3}}}";

        currentRound.setRoundPoints(roundPointsJson);

        when(roundRepository.findTopByGameIdOrderByIdDesc(gameId)).thenReturn(Optional.of(currentRound));

        // Act
        boolean allVotesSubmitted = roundService.areAllVotesSubmitted(gameId);

        // Assert
        assertTrue(allVotesSubmitted, "All votes should be submitted since each player has submitted for each category.");

        // Verify interactions
        verify(roundRepository).findTopByGameIdOrderByIdDesc(gameId);
    }

     */

    @Test
    public void testPrepareScoreAdjustments() throws Exception {
        // Setup
        Long gameId = 1L;
        Round currentRound = new Round();
        String roundPointsJson = "{\"category1\":{\"player1\":{\"score\":1, \"vetoVotes\":0, \"bonusVotes\":0}, \"player2\":{\"score\":1, \"vetoVotes\":0, \"bonusVotes\":0}}}";
        currentRound.setRoundPoints(roundPointsJson);

        when(roundRepository.findTopByGameIdOrderByIdDesc(gameId)).thenReturn(Optional.of(currentRound));

        // Prepare the expected data structure after reading the JSON
        Map<String, Map<String, Map<String, Object>>> scoresMap = new HashMap<>();
        Map<String, Map<String, Object>> categoryMap = new HashMap<>();
        Map<String, Object> player1Scores = new HashMap<>();
        player1Scores.put("score", 1);
        player1Scores.put("vetoVotes", 0);
        player1Scores.put("bonusVotes", 0);
        categoryMap.put("player1", player1Scores);
        Map<String, Object> player2Scores = new HashMap<>();
        player2Scores.put("score", 1);
        player2Scores.put("vetoVotes", 0);
        player2Scores.put("bonusVotes", 0);
        categoryMap.put("player2", player2Scores);
        scoresMap.put("category1", categoryMap);

        TypeReference<Map<String, Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {};

        // Adjustment map simulating user votes
        HashMap<String, HashMap<String, HashMap<String, Object>>> adjustments = new HashMap<>();
        adjustments.put("category1", new HashMap<String, HashMap<String, Object>>() {{
            put("player1", new HashMap<String, Object>() {{
                put("veto", true);
                put("bonus", true);
            }});
            put("player2", new HashMap<String, Object>() {{
                put("veto", false);
                put("bonus", true);
            }});
        }});

        // Execute
        Map<String, Map<String, Map<String, Object>>> updatedScores = roundService.prepareScoreAdjustments(gameId, adjustments);

        // Verify the updates to the round points
        assertNotNull(updatedScores);
        assertEquals(1, (int) updatedScores.get("category1").get("player1").get("vetoVotes"));
        assertEquals(1, (int) updatedScores.get("category1").get("player1").get("bonusVotes"));
        assertEquals(0, (int) updatedScores.get("category1").get("player2").get("vetoVotes"));
        assertEquals(1, (int) updatedScores.get("category1").get("player2").get("bonusVotes"));

        verify(roundRepository).save(any(Round.class));
    }

    @Test
    void getCurrentRoundLetterPosition_withValidGameIdAndRounds_shouldReturnLetterPosition() {
        Long gameId = 1L;
        Game game = new Game();
        Round round1 = new Round();
        round1.setLetterPosition(2);
        Round round2 = new Round();
        round2.setLetterPosition(5);
        List<Round> rounds = new ArrayList<>();
        rounds.add(round1);
        rounds.add(round2);
        game.setRounds(rounds);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        int letterPosition = roundService.getCurrentRoundLetterPosition(gameId);
        assertEquals(5, letterPosition);
    }

    @Test
    void getCurrentRoundLetterPosition_withValidGameIdAndNoRounds_shouldReturnNegativeHundred() {
        Long gameId = 1L;
        Game game = new Game();
        game.setRounds(new ArrayList<>());

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        int letterPosition = roundService.getCurrentRoundLetterPosition(gameId);
        assertEquals(-100, letterPosition);
    }

    @Test
    void getCurrentRoundLetterPosition_withInvalidGameId_shouldReturnNegativeHundred() {
        Long gameId = 999L;

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        int letterPosition = roundService.getCurrentRoundLetterPosition(gameId);
        assertEquals(-100, letterPosition);
    }





    @Test
    void calculateLevel_shouldReturnCorrectLevel() {
        assertEquals(0, roundService.calculateLevel(24)); // 24 is below the threshold for level 1
        assertEquals(1, roundService.calculateLevel(50)); // 50 falls in level 1: 25 * 1^2 = 25 to 100 (exclusive)
        assertEquals(2, roundService.calculateLevel(200)); // 200 falls in level 2: 25 * 2^2 = 100 to 225 (exclusive)
        assertEquals(4, roundService.calculateLevel(400)); // 400 falls in level 3: 25 * 3^2 = 225 to 400 (exclusive)
        assertEquals(5, roundService.calculateLevel(625)); // 625 falls in level 4: 25 * 4^2 = 400 to 625 (exclusive)
    }
}