package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.service.RoundService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;

class RoundControllerTest {
    private MockMvc mockMvc;

    @Mock
    private RoundService roundService;

    @MockBean
    private GameRepository gameRepository;

    @InjectMocks
    private RoundController roundController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        this.objectMapper = new ObjectMapper(); // Create a new ObjectMapper
        roundController.setObjectMapper(objectMapper); // Manually inject ObjectMapper
        mockMvc = MockMvcBuilders.standaloneSetup(roundController).build();
    }

    @Test
    void getRoundsByGameId_ShouldReturnRounds_WhenFound() throws Exception {
        List<Round> rounds = Arrays.asList(new Round(), new Round());
        when(roundService.getRoundByGameId(1L)).thenReturn(rounds);

        mockMvc.perform(get("/rounds/{gameId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(roundService).getRoundByGameId(1L);
    }

    @Test
    void getRoundsByGameId_ShouldReturnNotFound_WhenEmpty() throws Exception {
        when(roundService.getRoundByGameId(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/rounds/{gameId}", 1L))
                .andExpect(status().isNotFound());

        verify(roundService).getRoundByGameId(1L);
    }
    @Test
    void getRoundsByGameId_ShouldReturnNotFound_WhenGameIdIsInvalid() throws Exception {
        Long invalidGameId = -1L; // Assuming negative IDs are invalid
        when(roundService.getRoundByGameId(invalidGameId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/rounds/{gameId}", invalidGameId))
                .andExpect(status().isNotFound());

        verify(roundService).getRoundByGameId(invalidGameId);
    }
    @Test
    void getRoundsByGameId_ShouldReturnRounds_WhenRoundsExist() throws Exception {
        List<Round> rounds = List.of(new Round(), new Round());
        when(roundService.getRoundByGameId(1L)).thenReturn(rounds);

        mockMvc.perform(get("/rounds/{gameId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getRoundsByGameId_ShouldReturnNotFound_WhenRoundsDoNotExist() throws Exception {
        when(roundService.getRoundByGameId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/rounds/{gameId}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLetter_ShouldReturnNotFound_WhenLetterIsNotAssigned() throws Exception {
        when(roundService.getCurrentRoundLetter(1L)).thenReturn('\0');

        mockMvc.perform(get("/rounds/letters/{gameId}", 1L))
                .andExpect(status().isNotFound());
    }
    @Test
    void getScoresByCategory_ShouldReturnScores_WhenDataExists() throws Exception {
        Map<String, Map<String, Map<String, Object>>> scores = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        details.put("score", 100);
        Map<String, Map<String, Object>> category = new HashMap<>();
        category.put("Trivia", details);
        scores.put("user1", category);

        when(roundService.calculateScoresCategory(1L)).thenReturn(scores);

        mockMvc.perform(get("/rounds/scores/{gameId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user1.Trivia.score").value(100));
    }

    @Test
    void getScoresByCategory_ShouldReturnNotFound_WhenRuntimeException() throws Exception {
        when(roundService.calculateScoresCategory(1L)).thenThrow(new RuntimeException("Data not found"));

        mockMvc.perform(get("/rounds/scores/{gameId}", 1L))
                .andExpect(status().isNotFound());
    }


    @Test
    void getScoresByCategory_ShouldReturnServerError_WhenException() throws Exception {
        when(roundService.calculateScoresCategory(1L)).thenThrow(new Exception("Internal server error"));

        mockMvc.perform(get("/rounds/scores/{gameId}", 1L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void submitVotes_InvalidJson_ShouldReturnBadRequest() throws Exception {
        String rawJson = "{\"category1\":{\"user1\":{\"score\":}}"; // Malformed JSON

        mockMvc.perform(MockMvcRequestBuilders.post("/rounds/1/submitVotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void addGameEntry_ShouldReturnOk_WhenEntryAdded() throws Exception {
        Round currentRound = new Round();
        currentRound.setPlayerAnswers("[]");

        when(roundService.getCurrentRoundByGameId(1L)).thenReturn(currentRound);
        doNothing().when(roundService).saveRound(any(Round.class));

        HashMap<String, String> gameEntry = new HashMap<>();
        gameEntry.put("username", "user");
        gameEntry.put("answer", "apple");

        mockMvc.perform(post("/rounds/{gameId}/entries", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameEntry)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Entry added successfully.\"}"));

        verify(roundService).saveRound(any(Round.class));
    }
    @Test
    void addGameEntry_ShouldReturnNotFound_WhenNoCurrentRound() throws Exception {
        when(roundService.getCurrentRoundByGameId(1L)).thenReturn(null);

        HashMap<String, String> gameEntry = new HashMap<>();
        gameEntry.put("username", "user");
        gameEntry.put("answer", "apple");

        mockMvc.perform(post("/rounds/{gameId}/entries", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameEntry)))
                .andExpect(status().isNotFound());
    }
    @Test
    void getScoreDifference_ReturnsScoreDifference_WhenValid() throws Exception {
        Long gameId = 1L;
        Map<String, Integer> scoreDifference = Map.of("player1", 10, "player2", -5);

        when(roundService.calculateScoreDifference(gameId)).thenReturn(scoreDifference);

        mockMvc.perform(get("/rounds/score-difference/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player1").value(10))
                .andExpect(jsonPath("$.player2").value(-5));
    }
    @Test
    void getScoreDifference_ReturnsNotFound_WhenRuntimeExceptionThrown() throws Exception {
        Long gameId = 1L;
        when(roundService.calculateScoreDifference(gameId)).thenThrow(new RuntimeException("Error processing request"));

        mockMvc.perform(get("/rounds/score-difference/{gameId}", gameId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{}"));
    }


    @Test
    void getLeaderboard_ShouldReturnLeaderboard_WhenFound() throws Exception {
        long gameId = 1L;
        Game game = new Game();
        game.setGamePoints("{\"player1\": 50, \"player2\": 30}");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        mockMvc.perform(get("/rounds/leaderboard/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player1").value(50))
                .andExpect(jsonPath("$.player2").value(30));

        verify(gameRepository).findById(gameId);
    }

    @Test
    void getLeaderboard_ShouldReturnNotFound_WhenGameNotFound() throws Exception {
        long gameId = 1L;
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/rounds/leaderboard/{gameId}", gameId))
                .andExpect(status().isNotFound());

        verify(gameRepository).findById(gameId);
    }

    @Test
    void getLeaderboard_ShouldReturnInternalServerError_WhenExceptionThrown() throws Exception {
        long gameId = 1L;
        Game game = new Game();
        game.setGamePoints("{invalid json}");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        mockMvc.perform(get("/rounds/leaderboard/{gameId}", gameId))
                .andExpect(status().isInternalServerError());

        verify(gameRepository).findById(gameId);
    }
}

    // Additional tests for getLetter, getLeaderboard, getScoresByCategory should follow similar patterns