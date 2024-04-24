package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs24.service.RoundService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class RoundController {

    private final RoundService roundService;

    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }
    @GetMapping("/rounds/{gameId}")
    public ResponseEntity<List<Round>> getRoundsByGameId(@PathVariable Long gameId) {
        List<Round> rounds = roundService.getRoundByGameId(gameId);
        if (rounds != null && !rounds.isEmpty()) {
            return new ResponseEntity<>(rounds, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @Autowired
    private ObjectMapper objectMapper;
    @PostMapping("/rounds/{gameId}/entries")

    public ResponseEntity<String> addGameEntry(@PathVariable Long gameId, @RequestBody Map<String, String> gameEntry) {
        try {
            Round currentRound = roundService.getCurrentRoundByGameId(gameId);
            if (currentRound != null) {
                String entryJson = objectMapper.writeValueAsString(gameEntry);
                String existingAnswers = currentRound.getPlayerAnswers();
                String updatedAnswers = existingAnswers == null ? entryJson : existingAnswers + "," + entryJson;
                currentRound.setPlayerAnswers(updatedAnswers);
                roundService.saveRound(currentRound);
                return ResponseEntity.ok("{\"message\":\"Entry added successfully.\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"Failed to serialize entry.\"}");
        }
    }

    @GetMapping("/rounds/letters/{gameId}")
    public ResponseEntity<Character> getLetter(@PathVariable Long gameId) {
        char currentLetter = roundService.getCurrentRoundLetter(gameId);
        if (currentLetter != '\0') {
            return new ResponseEntity<>(currentLetter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/rounds/leaderboard/{gameId}")
    public ResponseEntity<Map<String, Integer>> getLeaderboard(@PathVariable Long gameId) {
        try {
            Map<String, Integer> leaderboard = roundService.calculateLeaderboard(gameId);
            return ResponseEntity.ok(leaderboard);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/rounds/scores/{gameId}")
    public ResponseEntity<?> getScoresByCategory(@PathVariable Long gameId) {
        try {
            Map<String, Map<String, Map<String, Object>>> scoresAndAnswersByCategory = roundService.calculateScoresCategory(gameId);
            return ResponseEntity.ok(scoresAndAnswersByCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
        /* Working function!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    commented out because not part of m3 and not shure if it adds bugs
    @PostMapping("/rounds/{gameId}/submitVotes")
    public ResponseEntity<?> submitVotes(@PathVariable Long gameId, @RequestBody String rawJson) {
        try {
            // First, parse the raw JSON into a simpler structure or directly into the desired complex structure
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<HashMap<String, HashMap<String, HashMap<String, Object>>>> typeRef =
                    new TypeReference<>() {};
            HashMap<String, HashMap<String, HashMap<String, Object>>> votes = objectMapper.readValue(rawJson, typeRef);

            // Proceed with using the parsed 'votes' as before
            Map<String, Map<String, Map<String, Object>>> updatedScores = roundService.adjustScores(gameId, votes);
            return ResponseEntity.ok(updatedScores);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Invalid JSON format: " + e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();  // Log runtime exceptions to help diagnose issues.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();  // Log unexpected exceptions.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    */
}
