package ch.uzh.ifi.hase.soprafs24;

// Corrected import for TypeReference
import com.fasterxml.jackson.core.type.TypeReference;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.service.RoundService;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.core.JsonProcessingException;

@Controller
public class WebSocketController {

    private final Set<String> readyPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> connectedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final SimpMessagingTemplate messagingTemplate;
    private final List<String> allPlayerAnswers = Collections.synchronizedList(new ArrayList<>());
    private final RoundService roundService;
    private final RoundRepository roundRepository;
    private final ObjectMapper objectMapper;

    // Autowired constructor for injecting services and repositories
    @Autowired
    public WebSocketController(RoundService roundService, RoundRepository roundRepository, ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate; // Ensure messagingTemplate is initialized
        this.roundService = roundService;
        this.roundRepository = roundRepository;
        this.objectMapper = objectMapper;
    }
    public WebSocketController(SimpMessagingTemplate messagingTemplate, RoundService roundService, RoundRepository roundRepository, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.roundService = roundService;
        this.roundRepository = roundRepository;
        this.objectMapper = objectMapper;
    }

    @MessageMapping("/connect")
    public void connectPlayer(@Payload Map<String, String> payload) {
        String username = payload.get("username");
        connectedPlayers.add(username);
    }

    @MessageMapping("/disconnect")
    public void disconnectPlayer(@Payload Map<String, String> payload) {
        String username = payload.get("username");
        connectedPlayers.remove(username);
        readyPlayers.remove(username);
        sendOnlineAndReadyCount(); // Update all clients when a player disconnects
    }

    @MessageMapping("/ready-up")
    @SendTo("/topic/ready-count") // Specify the destination for the response
    public String readyUp(@Payload Map<String, String> payload) {
        String username = payload.get("username");
        String lobbyIdStr = payload.get("lobbyId"); // Get the lobby ID from the payload
        long lobbyId = 0; // Initialize lobby ID
        try {
            lobbyId = Long.parseLong(lobbyIdStr); // Parse the lobby ID
        } catch (NumberFormatException e) {
            System.out.println("Error parsing lobby ID: " + lobbyIdStr);
            return "{\"error\":\"Invalid lobby ID\"}"; // Return an error message if parsing fails
        }

        if (connectedPlayers.contains(username)) {
            readyPlayers.add(username);
            sendOnlineAndReadyCount(username);
            if (checkAndStartGame()) {
                roundService.startNewRound(lobbyId); // Start new round with the parsed lobby ID
                return "{\"command\":\"start\"}";
            } else {
                return sendOnlineAndReadyCount(username);
            }
        } else {
            // Player not found in connected players
            return sendOnlineAndReadyCount(username);
        }
    }


    private Boolean checkAndStartGame() {
        // Check if all connected players are ready
        if (connectedPlayers.size() == readyPlayers.size()) {
            // If all players are ready, start the game
            return true;
        }
        else {
            // Not all players are ready, send online and ready count to the player

            return false;
        }
    }

    private String  sendOnlineAndReadyCount(String username) {
        // Send a message to the specified player with online and ready count
        int onlineCount = connectedPlayers.size();
        int readyCount = readyPlayers.size();
        String countMessage = "Online players: " + onlineCount + ", Ready players: " + readyCount;
       return  countMessage;
    }

    @MessageMapping("/start-game")
    @SendTo("/topic/game-control")
    public String startGame(){
        if (connectedPlayers.size() == readyPlayers.size()) {
            return "{\"command\":\"start\"}";
        } else {
            return "{\"error\":\"Not all connected players are ready\"}";
        }
    }

    @MessageMapping("/refresh")
    @SendTo("/topic/lobby-refresh")
    public String lobbyJoin(){
        return "{\"command\":\"refresh\"}";
    }

    @MessageMapping("/stop-game")
    @SendTo("/topic/game-control")
    public String stopGame() {
        readyPlayers.clear();
        return "{\"command\":\"stop\"}";
    }

    @MessageMapping("/not-ready")
    public void notReady(@Payload Map<String, String> payload) {
        String username = payload.get("username");
        readyPlayers.remove(username);
    }

    private String sendOnlineAndReadyCount() {
        int onlineCount = connectedPlayers.size();
        int readyCount = readyPlayers.size();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("onlinePlayers", onlineCount);
        jsonObject.put("readyPlayers", readyCount);

        return jsonObject.toString();
    }
    @MessageMapping("/game-entries")
    @SendTo("/topic/game-over")
    public String receiveGameEntries(@Payload Map<String, String> gameEntry) {
        String playerIdentifier = gameEntry.remove("username");
        String answer = gameEntry.get("answer"); // Assuming each entry includes an "answer" key

        if (!readyPlayers.contains(playerIdentifier)) {
            System.out.println("Received submission from unready or unknown player: " + playerIdentifier);
            return "{\"command\":\"entities\"}";
        }

        allPlayerAnswers.add(answer); // Append answer to the list
        updateRoundWithAnswers();
        return "{\"command\":\"entities\"}";
    }

    // Method to get all player answers
    @MessageMapping("/leaderboard")
    @SendTo("/topic/leaderboard")
    public String getLeaderboard() {
        return "{\"command\":\"Leaderboard\"}"; // You can customize this JSON message as needed.
    }

    private void updateRoundWithAnswers() {
        Round currentRound = roundService.getCurrentRound(); // Assuming you have a method to get the current round
        try {
            // Convert current list of answers to JSON string
            String jsonAnswers = objectMapper.writeValueAsString(allPlayerAnswers);
            currentRound.setPlayerAnswers(jsonAnswers);
            roundRepository.save(currentRound);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("Failed to update round with answers");
        }
    }
}
