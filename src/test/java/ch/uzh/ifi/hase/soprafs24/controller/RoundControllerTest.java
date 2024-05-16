package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.service.RoundService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

class RoundControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoundService roundService;

    @InjectMocks
    private RoundController roundController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this); // Initialize mocks
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
/*
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
*/
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
    void addGameEntry_ShouldReturnNotFound_WhenRoundDoesNotExist() throws Exception {
        when(roundService.getCurrentRoundByGameId(1L)).thenReturn(null);

        mockMvc.perform(post("/rounds/{gameId}/entries", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }


    @Test
    void getLetter_ShouldReturnNotFound_WhenLetterIsNotAssigned() throws Exception {
        when(roundService.getCurrentRoundLetter(1L)).thenReturn('\0');

        mockMvc.perform(get("/rounds/letters/{gameId}", 1L))
                .andExpect(status().isNotFound());
    }


    @Test
    void getLeaderboard_ShouldReturnData_WhenDataExists() throws Exception {
        Map<String, Integer> leaderboard = new HashMap<>();
        leaderboard.put("user1", 150);
        when(roundService.calculateLeaderboard(1L)).thenReturn(leaderboard);

        mockMvc.perform(get("/rounds/leaderboard/{gameId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user1").value(150));
    }
    @Test
    void getLeaderboard_ShouldReturnNotFound_WhenRuntimeExceptionThrown() throws Exception {
        when(roundService.calculateLeaderboard(1L)).thenThrow(new RuntimeException());

        mockMvc.perform(get("/rounds/leaderboard/{gameId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getLeaderboard_ShouldReturnInternalServerError_WhenExceptionThrown() throws Exception {
        when(roundService.calculateLeaderboard(1L)).thenThrow(new Exception());

        mockMvc.perform(get("/rounds/leaderboard/{gameId}", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist());
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

    /*
    @Test
    void submitVotes_ValidRequest_ShouldReturnUpdatedScores() throws Exception {
        String rawJson = "{\"category1\":{\"user1\":{\"score\": 10}}}";
        HashMap<String, HashMap<String, HashMap<String, Object>>> votes = new HashMap<>();
        HashMap<String, HashMap<String, Object>> userDetails = new HashMap<>();
        HashMap<String, Object> scoreDetails = new HashMap<>();
        scoreDetails.put("score", 10);
        userDetails.put("user1", scoreDetails);
        votes.put("category1", userDetails);

        given(roundService.adjustScores(1L, votes)).willReturn(new HashMap<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/rounds/1/submitVotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

     */

    @Test
    void submitVotes_InvalidJson_ShouldReturnBadRequest() throws Exception {
        String rawJson = "{\"category1\":{\"user1\":{\"score\":}}"; // Malformed JSON

        mockMvc.perform(MockMvcRequestBuilders.post("/rounds/1/submitVotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /*
    @Test
    void submitVotes_ServiceThrowsRuntimeException_ShouldReturnNotFound() throws Exception {
        String rawJson = "{\"category1\":{\"user1\":{\"score\": 10}}}";
        given(roundService.adjustScores(anyLong(), any())).willThrow(new RuntimeException("Game not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/rounds/1/submitVotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Game not found"));
    }

     */

    @Test
    void addGameEntry_ExistingRound_ShouldAddSuccessfully() throws Exception {
        Long gameId = 1L;
        Map<String, String> gameEntry = Map.of("playerId", "123", "answer", "apple");
        String entryJson = objectMapper.writeValueAsString(gameEntry);
        Round mockRound = new Round();
        mockRound.setPlayerAnswers(null);  // Simulating no existing answers

        given(roundService.getCurrentRoundByGameId(gameId)).willReturn(mockRound);

        mockMvc.perform(MockMvcRequestBuilders.post("/rounds/{gameId}/entries", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entryJson))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"message\":\"Entry added successfully.\"}"));

        verify(roundService, times(1)).saveRound(any(Round.class));
    }

    @Test
    void addGameEntry_RoundNotFound_ShouldReturnNotFound() throws Exception {
        Long gameId = 1L;
        Map<String, String> gameEntry = Map.of("playerId", "123", "answer", "apple");
        String entryJson = objectMapper.writeValueAsString(gameEntry);

        given(roundService.getCurrentRoundByGameId(gameId)).willReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/rounds/{gameId}/entries", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entryJson))
                .andExpect(status().isNotFound());

    }

}

    // Additional tests for getLetter, getLeaderboard, getScoresByCategory should follow similar patterns

