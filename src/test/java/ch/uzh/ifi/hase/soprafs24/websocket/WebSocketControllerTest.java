package ch.uzh.ifi.hase.soprafs24.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.uzh.ifi.hase.soprafs24.WebSocketController;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;
import ch.uzh.ifi.hase.soprafs24.service.RoundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

}