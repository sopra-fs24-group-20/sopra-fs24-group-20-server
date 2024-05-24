package ch.uzh.ifi.hase.soprafs24;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.service.RoundService;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
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

    public final Map<Long, Set<String>> readyPlayers = new ConcurrentHashMap<>();
    public final Map<Long, Set<String>> connectedPlayers = new ConcurrentHashMap<>();
    public final Map<Long, Set<String>> submittedPlayers = new ConcurrentHashMap<>();
    public final Map<Long, Set<String>> gamePlayers = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final RoundService roundService;
    private final RoundRepository roundRepository;
    private final ObjectMapper objectMapper;

    private final LobbyRepository lobbyRepository;

    @Autowired
    public WebSocketController(RoundService roundService, RoundRepository roundRepository, ObjectMapper objectMapper, LobbyRepository lobbyRepository,SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.roundService = roundService;
        this.roundRepository = roundRepository;
        this.objectMapper = objectMapper;
        this.lobbyRepository = lobbyRepository;
    }

    @MessageMapping("/connect")
    public void connect(@Payload Map<String, String> payload) {
        // Handle connection logic if needed
    }

    @MessageMapping("/disconnect")
    public void disconnect(@Payload Map<String, String> payload) {
        // Handle disconnection logic if needed
    }

    @MessageMapping("/join")
    public void joinLobby(@Payload Map<String, String> payload) {
        String username = payload.get("username");
        Long lobbyId = Long.parseLong(payload.get("lobbyId"));
        connectedPlayers.computeIfAbsent(lobbyId, k -> new HashSet<>()).add(username);
        sendOnlineAndReadyCount(lobbyId);
    }

    @MessageMapping("/leave")
    public void leaveLobby(@Payload Map<String, String> payload) {
        String username = payload.get("username");
        Long lobbyId = Long.parseLong(payload.get("lobbyId"));
        connectedPlayers.getOrDefault(lobbyId, Collections.emptySet()).remove(username);
        readyPlayers.getOrDefault(lobbyId, Collections.emptySet()).remove(username);
        submittedPlayers.getOrDefault(lobbyId, Collections.emptySet()).remove(username);
        sendOnlineAndReadyCount(lobbyId);
        if (checkallAnswers(lobbyId)){
            String startMSG = String.format("{\"command\":\"done\", \"lobbyId\":" + lobbyId + "}");
            messagingTemplate.convertAndSend("/topic/answers-count", startMSG);
        }
        if (checkAndStartGame(lobbyId)) {
            try {
                roundService.startNewRound(lobbyId);
                String startMSG = String.format("{\"command\":\"start\", \"lobbyId\":" + lobbyId + "}");
                messagingTemplate.convertAndSend("/topic/ready-count", startMSG);
            } catch (Exception e) {
                System.out.println("Failed to start new round for lobby " + lobbyId + ": " + e.getMessage());
                String erroroMSG=String.format( "{\"error\":\"Failed to start game for lobby " + lobbyId + ": " + e.getMessage() + "\"}");
                messagingTemplate.convertAndSend("/topic/ready-count", erroroMSG);
            }
        } else {
            sendOnlineAndReadyCount(lobbyId);
        }
    }

    @MessageMapping("/ready-up")
    @SendTo("/topic/ready-count")
    public String readyUp(@Payload Map<String, String> payload) {
        String username = payload.get("username");
        Long lobbyId;

        try {
            lobbyId = Long.parseLong(payload.get("lobbyId"));
        } catch (NumberFormatException e) {
            System.out.println("Error parsing lobby ID from: " + payload.get("lobbyId") + ", Error: " + e.getMessage());
            return "{\"error\":\"Invalid lobby ID\"}";
        }

        Set<String> lobbyConnected = connectedPlayers.computeIfAbsent(lobbyId, k -> ConcurrentHashMap.newKeySet());
        Set<String> lobbyReady = readyPlayers.computeIfAbsent(lobbyId, k -> ConcurrentHashMap.newKeySet());

        if (!lobbyConnected.contains(username)) {
            System.out.println("User " + username + " not found in lobby " + lobbyId);
            return "{\"error\":\"User " + username + " not found in lobby " + lobbyId + "\"}";
        }

        lobbyReady.add(username);
        if (checkAndStartGame(lobbyId)) {
            try {
                roundService.startNewRound(lobbyId);
                return "{\"command\":\"start\", \"lobbyId\":" + lobbyId + "}";
            } catch (Exception e) {
                System.out.println("Failed to start new round for lobby " + lobbyId + ": " + e.getMessage());
                return "{\"error\":\"Failed to start game for lobby " + lobbyId + ": " + e.getMessage() + "\"}";
            }
        } else {
            return sendOnlineAndReadyCount(lobbyId);
        }
    }

    // after eval screen to get to leaderboard
    @MessageMapping("/answers-submitted")
    @SendTo("/topic/answers-count")
    public String answers(@Payload Map<String, String> payload) throws Exception {
        String username = payload.get("username");
        Long lobbyId;

        try {
            lobbyId = Long.parseLong(payload.get("lobbyId"));
        } catch (NumberFormatException e) {
            System.out.println("Error parsing lobby ID from: " + payload.get("lobbyId") + ", Error: " + e.getMessage());
            return "{\"error\":\"Invalid lobby ID\"}";
        }

        Set<String> lobbyConnected = connectedPlayers.computeIfAbsent(lobbyId, k -> ConcurrentHashMap.newKeySet());
        Set<String> answersSubmitted = submittedPlayers.computeIfAbsent(lobbyId, k -> ConcurrentHashMap.newKeySet());

        if (!lobbyConnected.contains(username)) {
            System.out.println("User " + username + " not found in lobby " + lobbyId);
            return "{\"error\":\"User " + username + " not found in lobby " + lobbyId + "\"}";
        }

        answersSubmitted.add(username);
        if (checkallAnswers(lobbyId)) {
            Optional<Lobby> lobbyOptional = lobbyRepository.findById(lobbyId);
            if (lobbyOptional.isEmpty()) {
                return "{\"error\":\"Invalid lobby ID\"}";
            }

            Lobby lobby = lobbyOptional.get();
            Game game = lobby.getGame();
            Long gameId = game.getId();
            lobbyRepository.save(lobby);
            roundService.calculateFinalScores(gameId);

            try {
                roundService.calculateLeaderboard(lobbyId);
            } catch (Exception e) {
                System.out.println("Error calculating leaderboard for lobby " + lobbyId + ": " + e.getMessage());
            }

            return "{\"command\":\"done\", \"lobbyId\":" + lobbyId + "}";
        } else {
            return String.format("{\"error\":\"Not all connected players have submitted\", \"lobbyId\":%d}", lobbyId);
        }
    }

    public Boolean checkallAnswers(Long lobbyId) {
        Set<String> lobbyConnected = connectedPlayers.getOrDefault(lobbyId, Collections.emptySet());
        Set<String> answersSubmitted = submittedPlayers.getOrDefault(lobbyId, Collections.emptySet());
        // Start the leaderboard if all connected players are ready, or if there is only one player who is ready.
        if (lobbyConnected.size() == 0){
            submittedPlayers.remove(lobbyId);
            return false;
        }
        if ((lobbyConnected.size() != 0 && (lobbyConnected.equals(answersSubmitted) || (lobbyConnected.size() == 1 && answersSubmitted.size() == 1)))) {
            submittedPlayers.remove(lobbyId);
            return true;
        }
        return false;
    }


    // after game to get to eval screen
    @MessageMapping("/game-submitted")
    @SendTo("/topic/game-answers")
    public String game(@Payload Map<String, String> payload) {
        String username = payload.get("username");
        Long lobbyId;

        try {
            lobbyId = Long.parseLong(payload.get("lobbyId"));
        } catch (NumberFormatException e) {
            System.out.println("Error parsing lobby ID from: " + payload.get("lobbyId") + ", Error: " + e.getMessage());
            return "{\"error\":\"Invalid lobby ID\"}";
        }

        Set<String> lobbyConnected = connectedPlayers.computeIfAbsent(lobbyId, k -> ConcurrentHashMap.newKeySet());
        Set<String> gameSubmitted = gamePlayers.computeIfAbsent(lobbyId, k -> ConcurrentHashMap.newKeySet());

        if (!lobbyConnected.contains(username)) {
            System.out.println("User " + username + " not found in lobby " + lobbyId);
            return "{\"error\":\"User " + username + " not found in lobby " + lobbyId + "\"}";
        }

        gameSubmitted.add(username);
        if (checkgameAnswers(lobbyId)) {
            return "{\"command\":\"game-done\", \"lobbyId\":" + lobbyId + "}";
        } else {
            return String.format("{\"error\":\"Not all connected players are ready\", \"lobbyId\":%d}", lobbyId);
        }
    }

    private Boolean checkgameAnswers(Long lobbyId) {
        Set<String> lobbyConnected = connectedPlayers.getOrDefault(lobbyId, Collections.emptySet());
        Set<String> gameSubmitted = gamePlayers.getOrDefault(lobbyId, Collections.emptySet());
        // Start the evaluation if all connected players are ready, or if there is only one player who is ready.
        if (lobbyConnected.size() == 0){
            gamePlayers.remove(lobbyId);
            return false;
        }
        if (lobbyConnected.equals(gameSubmitted) || (lobbyConnected.size() == 1 && gameSubmitted.size() == 1)){
            gamePlayers.remove(lobbyId);
            return true;
        }
        return false;
    }


    private Boolean checkAndStartGame(Long lobbyId) {
        Set<String> lobbyConnected = connectedPlayers.getOrDefault(lobbyId, Collections.emptySet());
        Set<String> lobbyReady = readyPlayers.getOrDefault(lobbyId, Collections.emptySet());
        // Start the game if all connected players are ready, or if there is only one player who is ready.
        if (lobbyConnected.size() == 0){
            return false;
        }
        return lobbyConnected.equals(lobbyReady) || (lobbyConnected.size() == 1 && lobbyReady.size() == 1);
    }


    private String sendOnlineAndReadyCount(Long lobbyId) {
        int onlineCount = connectedPlayers.getOrDefault(lobbyId, Collections.emptySet()).size();
        int readyCount = readyPlayers.getOrDefault(lobbyId, Collections.emptySet()).size();
        String countMessage = String.format("{\"onlinePlayers\": %d, \"readyPlayers\": %d, \"lobbyId\": %d}", onlineCount, readyCount, lobbyId);
        messagingTemplate.convertAndSend("/topic/online-players", countMessage);
        return countMessage;
    }

    @MessageMapping("/start-game")
    @SendTo("/topic/game-control")
    public String startGame(@Payload Map<String, String> payload) {
        Long lobbyId = Long.parseLong(payload.get("lobbyId"));
        if (checkAndStartGame(lobbyId)) {
            return String.format("{\"command\":\"start\", \"lobbyId\":%d}", lobbyId);
        } else {
            return String.format("{\"error\":\"Not all connected players are ready\", \"lobbyId\":%d}", lobbyId);
        }
    }

    @MessageMapping("/stop-game")
    @SendTo("/topic/game-control")
    public String stopGame(@Payload Map<String, String> payload) {
        Long lobbyId = Long.parseLong(payload.get("lobbyId"));
        readyPlayers.remove(lobbyId);
        return String.format("{\"command\":\"stop\", \"lobbyId\":%d}", lobbyId);
    }

    @MessageMapping("/not-ready")
    public void notReady(@Payload Map<String, String> payload) {
        String username = payload.get("username");
        Long lobbyId = Long.parseLong(payload.get("lobbyId"));
        readyPlayers.getOrDefault(lobbyId, Collections.emptySet()).remove(username);
        sendOnlineAndReadyCount(lobbyId);
    }

    @MessageMapping("/game-entries")
    @SendTo("/topic/game-over")
    public String receiveGameEntries(@Payload Map<String, String> gameEntry) {
        String username = gameEntry.remove("username");
        Long lobbyId = Long.parseLong(gameEntry.get("lobbyId"));
        Long gameId = Long.parseLong(gameEntry.get("gameId"));
        if (!readyPlayers.getOrDefault(lobbyId, Collections.emptySet()).contains(username)) {
            return String.format("{\"command\":\"entities\", \"lobbyId\":%d}", lobbyId);
        }
        // Assume allPlayerAnswers is a Map<Long, List<String>> keyed by lobbyId
        updateRoundWithAnswers(gameId, lobbyId);
        return String.format("{\"command\":\"entities\", \"lobbyId\":%d}", lobbyId);
    }

    public void updateRoundWithAnswers(Long gameId, Long lobbyId) {
        Round currentRound = roundService.getCurrentRound(gameId);
        try {
            // Assuming allPlayerAnswers is defined elsewhere and keyed by lobbyId
            String jsonAnswers = objectMapper.writeValueAsString(Collections.emptyList()); // Placeholder
            currentRound.setPlayerAnswers(jsonAnswers);
            roundRepository.save(currentRound);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("Failed to update round with answers");
        }
    }

    /*@MessageMapping("/submit-votes")
    @SendTo("/topic/final-scores")
    public String handleVoteSubmission(@Payload Map<String, String> payload) throws Exception {
        String username = payload.get("username");
        Long lobbyId;
        try {
            lobbyId = Long.parseLong(payload.get("lobbyId"));
        } catch (NumberFormatException e) {
            System.out.println("Error parsing lobby ID from: " + payload.get("lobbyId") + ", Error: " + e.getMessage());
            return "{\"error\":\"Invalid lobby ID\"}";
        }
        Optional<Lobby> optionalLobby = lobbyRepository.findById(lobbyId);
        if (optionalLobby.isEmpty()) {
            throw new RuntimeException("No current lobby for lobby ID: " + lobbyId);
        }

        Lobby lobby = optionalLobby.get();
        Long gameId = lobby.getGame().getId();
        HashMap<String, HashMap<String, HashMap<String, Object>>> votes = (HashMap<String, HashMap<String, HashMap<String, Object>>>) payload.get("votes");

        // Process the incoming votes and update the server state
        Map<String, Map<String, Map<String, Object>>> voteUpdates = roundService.prepareScoreAdjustments(gameId, votes);

        // Check if all players have submitted their votes
        if (roundService.areAllVotesSubmitted(gameId)) {
            // All votes submitted, proceed to calculate final scores
            // Map<String, Map<String, Map<String, Object>>> finalScores = roundService.calculateFinalScores(lobbyId);
            // messagingTemplate.convertAndSend("/topic/final-scores", finalScores);
            return String.format("{\"command\":\"final\", \"lobbyId\":%d}", lobbyId);
        } else {
            // Not all votes are in, send an update to clients
            //messagingTemplate.convertAndSend("/topic/vote-updates", voteUpdates);
            return String.format("{\"error\":\"Not all connected players have finished voting\", \"lobbyId\":%d}", lobbyId);
        }
    }*/

    /*@MessageMapping("/leaderboard")
    @SendTo("/topic/leaderboard")
    public String getLeaderboard(@Payload Map<String, String> payload) {
        Long lobbyId = Long.parseLong(payload.get("lobbyId"));
        return String.format("{\"command\":\"Leaderboard\", \"lobbyId\":%d}", lobbyId);
    }*/
}