package ch.uzh.ifi.hase.soprafs24.websocket;

import ch.uzh.ifi.hase.soprafs24.WebSocketController;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.service.RoundService;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void connectPlayer_ShouldAddUsername() {
        Map<String, String> payload = Map.of("username", "testUser");

        webSocketController.connectPlayer(payload);

        assertTrue(webSocketController.connectedPlayers.contains("testUser"));
    }

    @Test
    void disconnectPlayer_ShouldRemoveUsername() {
        Map<String, String> payload = Map.of("username", "testUser");
        webSocketController.connectedPlayers.add("testUser");
        webSocketController.readyPlayers.add("testUser");

        webSocketController.disconnectPlayer(payload);

        assertFalse(webSocketController.connectedPlayers.contains("testUser"));
        assertFalse(webSocketController.readyPlayers.contains("testUser"));
    }

    @Test
    void readyUp_PlayerNotConnected_ShouldNotAddToReady() {
        Map<String, String> payload = Map.of("username", "testUser", "lobbyId", "1");

        String result = webSocketController.readyUp(payload);

        assertFalse(webSocketController.readyPlayers.contains("testUser"));
        assertEquals("Online players: 0, Ready players: 0", result);
    }


    @Test
    void startGame_AllPlayersReady_ShouldStartGame() {
        webSocketController.connectedPlayers.add("user1");
        webSocketController.readyPlayers.add("user1");

        String result = webSocketController.startGame();

        assertEquals("{\"command\":\"start\"}", result);
    }

    @Test
    void startGame_NotAllPlayersReady_ShouldReturnError() {
        webSocketController.connectedPlayers.add("user1");
        webSocketController.connectedPlayers.add("user2");
        webSocketController.readyPlayers.add("user1");

        String result = webSocketController.startGame();

        assertEquals("{\"error\":\"Not all connected players are ready\"}", result);
    }
    @Test
    void stopGame_ShouldClearReadyPlayers() {
        webSocketController.readyPlayers.addAll(List.of("user1", "user2")); // Assume some users are ready

        String result = webSocketController.stopGame();

        assertTrue(webSocketController.readyPlayers.isEmpty(), "Ready players should be cleared");
        assertEquals("{\"command\":\"stop\"}", result, "The stop command should be returned");
    }
    @Test
    void notReady_ShouldRemoveUserFromReadyPlayers() {
        Map<String, String> payload = Map.of("username", "user1");
        webSocketController.readyPlayers.add("user1"); // Adding user to ready players

        webSocketController.notReady(payload);

        assertFalse(webSocketController.readyPlayers.contains("user1"), "User should be removed from ready players");
    }
    @Test
    void receiveGameEntries_PlayerNotReady_ShouldReturnError() {
        Map<String, String> gameEntry = new HashMap<>();
        gameEntry.put("username", "user1");
        gameEntry.put("answer", "SomeAnswer");

        String result = webSocketController.receiveGameEntries(gameEntry);

        assertEquals("{\"command\":\"entities\"}", result, "Should return default command when player not ready");
    }

    @Test
    void receiveGameEntries_PlayerReady_ShouldAddAnswerAndUpdateRound() throws JsonProcessingException {
        Map<String, String> gameEntry = new HashMap<>();
        gameEntry.put("username", "user1");
        gameEntry.put("answer", "SomeAnswer");
        webSocketController.readyPlayers.add("user1"); // Mark user as ready

        // Assuming you have a method getCurrentRound in RoundService
        Round mockRound = new Round();
        when(roundService.getCurrentRound()).thenReturn(mockRound);
        when(objectMapper.writeValueAsString(any())).thenReturn("jsonAnswers");

        String result = webSocketController.receiveGameEntries(gameEntry);

        assertEquals("{\"command\":\"entities\"}", result, "Should process the entry");
        verify(roundRepository, times(1)).save(any(Round.class));
        assertTrue(webSocketController.allPlayerAnswers.contains("SomeAnswer"), "Answer should be added to the list");
    }

    @Test
    void receiveGameEntries_JsonProcessingException_ShouldLogError() throws JsonProcessingException {
        Map<String, String> gameEntry = new HashMap<>();
        gameEntry.put("username", "user1");
        gameEntry.put("answer", "SomeAnswer");
        webSocketController.readyPlayers.add("user1"); // Mark user as ready

        // Assuming you have a method getCurrentRound in RoundService
        Round mockRound = new Round();
        when(roundService.getCurrentRound()).thenReturn(mockRound);
        doThrow(new JsonProcessingException("Test Exception") {}).when(objectMapper).writeValueAsString(any());

        String result = webSocketController.receiveGameEntries(gameEntry);

        assertEquals("{\"command\":\"entities\"}", result, "Should return default command on exception");
        verify(roundRepository, times(0)).save(any(Round.class));
        assertTrue(webSocketController.allPlayerAnswers.contains("SomeAnswer"), "Answer should still be added to the list");
    }

}
