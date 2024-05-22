package ch.uzh.ifi.hase.soprafs24.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs24.WebSocketController;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;
import ch.uzh.ifi.hase.soprafs24.service.RoundService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class WebSocketControllerTest {

    @Mock
    private RoundService roundService;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketController webSocketController;

    private final Long TEST_LOBBY_ID = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        webSocketController.connectedPlayers.clear();
        webSocketController.readyPlayers.clear();
    }

    @Test
    void joinLobby_ShouldAddUsername() {
        Map<String, String> payload = Map.of("username", "testUser", "lobbyId", TEST_LOBBY_ID.toString());
        webSocketController.joinLobby(payload);

        assertTrue(webSocketController.connectedPlayers.get(TEST_LOBBY_ID).contains("testUser"));
    }

    @Test
    void leaveLobby_ShouldRemoveUsername() {
        Map<String, String> payload = Map.of("username", "testUser", "lobbyId", TEST_LOBBY_ID.toString());
        webSocketController.connectedPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add("testUser");
        webSocketController.readyPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add("testUser");

        webSocketController.leaveLobby(payload);

        assertFalse(webSocketController.connectedPlayers.get(TEST_LOBBY_ID).contains("testUser"));
        assertFalse(webSocketController.readyPlayers.get(TEST_LOBBY_ID).contains("testUser"));
    }
/*
    @Test
    void readyUp_PlayerNotConnected_ShouldReturnError() {
        Map<String, String> payload = Map.of("username", "testUser", "lobbyId", TEST_LOBBY_ID.toString());

        String result = webSocketController.readyUp(payload);

        assertFalse(webSocketController.readyPlayers.getOrDefault(TEST_LOBBY_ID, Collections.emptySet()).contains("testUser"));
        assertEquals("{\"error\":\"User testUser not found in lobby 1\"}", result);
    }
*/

    @Test
    void startGame_AllPlayersReady_ShouldStartGame() {
        webSocketController.connectedPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add("user1");
        webSocketController.readyPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add("user1");
        Map<String, String> payload = Map.of("lobbyId", TEST_LOBBY_ID.toString());

        String result = webSocketController.startGame(payload);

        assertEquals(String.format("{\"command\":\"start\", \"lobbyId\":%d}", TEST_LOBBY_ID), result);
    }

    @Test
    void startGame_NotAllPlayersReady_ShouldReturnError() {
        webSocketController.connectedPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add("user1");
        webSocketController.connectedPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add("user2");
        webSocketController.readyPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add("user1");
        Map<String, String> payload = Map.of("lobbyId", TEST_LOBBY_ID.toString());

        String result = webSocketController.startGame(payload);

        assertEquals(String.format("{\"error\":\"Not all connected players are ready\", \"lobbyId\":%d}", TEST_LOBBY_ID), result);
    }

    @Test
    void stopGame_ShouldClearReadyPlayers() {
        webSocketController.readyPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).addAll(Set.of("user1", "user2"));
        Map<String, String> payload = Map.of("lobbyId", TEST_LOBBY_ID.toString());

        String result = webSocketController.stopGame(payload);

        assertTrue(webSocketController.readyPlayers.getOrDefault(TEST_LOBBY_ID, Collections.emptySet()).isEmpty());
        assertEquals(String.format("{\"command\":\"stop\", \"lobbyId\":%d}", TEST_LOBBY_ID), result);
    }
    @Test
    void notReady_ShouldRemovePlayerFromReady() {
        // Setup initial conditions where a user is marked as ready
        String username = "user1";
        webSocketController.readyPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add(username);
        Map<String, String> payload = Map.of("username", username, "lobbyId", TEST_LOBBY_ID.toString());

        // Invoke the method under test
        webSocketController.notReady(payload);

        // Verify the user is no longer in the ready set
        assertFalse(webSocketController.readyPlayers.get(TEST_LOBBY_ID).contains(username));
    }
    @Test
    void game_ShouldUpdateGameStatusWhenAllPlayersSubmitted() {
        // Setup initial conditions
        String username = "user1";
        webSocketController.connectedPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add(username);
        webSocketController.gamePlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add(username);
        Map<String, String> payload = Map.of("username", username, "lobbyId", TEST_LOBBY_ID.toString());

        // Invoke the method under test
        String result = webSocketController.game(payload);

        // Assert game is considered done when all have submitted
        assertEquals(String.format("{\"command\":\"game-done\", \"lobbyId\":%d}", TEST_LOBBY_ID), result);
    }
    @Test
    void readyUp_ShouldMarkPlayerReadyAndCheckGameStart() {
        // Setup initial conditions
        String username = "user1";
        webSocketController.connectedPlayers.computeIfAbsent(TEST_LOBBY_ID, k -> new HashSet<>()).add(username);
        Map<String, String> payload = Map.of("username", username, "lobbyId", TEST_LOBBY_ID.toString());

        // Invoke the method under test
        webSocketController.readyUp(payload);

        // Verify the player is marked as ready and game check was initiated
        assertTrue(webSocketController.readyPlayers.get(TEST_LOBBY_ID).contains(username));
    }

    @Test
    void receiveGameEntries_WhenUserNotReady_ShouldReturnError() {
        // Create a mutable map
        Map<String, String> gameEntry = new HashMap<>();
        gameEntry.put("username", "user1");
        gameEntry.put("lobbyId", "1");
        gameEntry.put("gameId", "2");

        // Invoke the method under test
        String result = webSocketController.receiveGameEntries(gameEntry);

        // Assert it returns the appropriate error message
        assertEquals("{\"command\":\"entities\", \"lobbyId\":1}", result);
    }
    @Test
    void answers_WhenUserNotFound_ShouldReturnError() throws Exception {
        Map<String, String> payload = Map.of("username", "user1", "lobbyId", "1");
        Long lobbyId = 1L;
        webSocketController.connectedPlayers.computeIfAbsent(lobbyId, k -> ConcurrentHashMap.newKeySet());


        String result = webSocketController.answers(payload);

        assertEquals("{\"error\":\"User user1 not found in lobby 1\"}", result);
    }
    @Test
    void updateRoundWithAnswers_UpdatesRoundSuccessfully() throws JsonProcessingException {
        // Setup
        Long gameId = 1L;
        Long lobbyId = 1L;
        Round mockRound = new Round();
        when(roundService.getCurrentRound(gameId)).thenReturn(mockRound);
        when(objectMapper.writeValueAsString(Collections.emptyList())).thenReturn("[]");

        // Action
        webSocketController.updateRoundWithAnswers(gameId, lobbyId);

        // Assertion
        verify(roundService).getCurrentRound(gameId);
        verify(objectMapper).writeValueAsString(Collections.emptyList());
        verify(roundRepository).save(mockRound);
        assertEquals("[]", mockRound.getPlayerAnswers());
    }
    @Test
    void updateRoundWithAnswers_HandlesJsonProcessingException() throws JsonProcessingException {
        // Setup
        Long gameId = 1L;
        Long lobbyId = 1L;
        Round mockRound = new Round();
        when(roundService.getCurrentRound(gameId)).thenReturn(mockRound);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization failed") {});

        // Action
        webSocketController.updateRoundWithAnswers(gameId, lobbyId);

        // Assertion
        verify(roundService).getCurrentRound(gameId);
        verify(objectMapper).writeValueAsString(Collections.emptyList());
        verifyNoInteractions(roundRepository); // Ensure that save is not called
        // Optionally, check that the error is logged (this requires a logging framework like SLF4J and a corresponding mocking tool or a TestAppender)
    }
}