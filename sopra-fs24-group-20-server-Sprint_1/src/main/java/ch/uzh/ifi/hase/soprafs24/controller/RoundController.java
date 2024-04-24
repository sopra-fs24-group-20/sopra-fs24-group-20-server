package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs24.service.RoundService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/round")
public class RoundController {

    private final RoundService roundService;

    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }
    @GetMapping("/{gameId}")
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

    @GetMapping("/answers/{gameId}")
    public ResponseEntity<String> getAnswersByGameId(@PathVariable Long gameId) {
        Round currentRound = roundService.getCurrentRoundByGameId(gameId);
        if (currentRound == null) {
            return ResponseEntity.notFound().build(); // Return 404 if no current round found
        }

        // Assuming PlayerCategoryResponse can be directly serialized and matches the frontend expectations
        List<Round.PlayerCategoryResponse> currentRoundAnswers = currentRound.getRoundAnswers().getOrDefault("username", new ArrayList<>());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(currentRoundAnswers);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        } catch (Exception e) {
            // Log the error and return an appropriate response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing JSON");
        }
    }


    @PutMapping("/answers/{gameId}/player")
    public ResponseEntity<?> updatePlayerAnswers(@PathVariable Long gameId, @RequestBody PlayerAnswersUpdateRequest request) {
        roundService.updatePlayerAnswers(gameId, request.getUsername(), request.getCategoryAnswers());
        return ResponseEntity.ok("Player answers updated successfully");
    }

    @PutMapping("/answers/{gameId}/all")
    public ResponseEntity<?> updateAllAnswers(@PathVariable Long gameId, @RequestBody Map<String, List<Round.PlayerCategoryResponse>> allAnswers) {
        roundService.updateAllAnswers(gameId, allAnswers);
        return ResponseEntity.ok("All answers updated successfully");
    }


    @GetMapping("/letters/{gameId}")
    public ResponseEntity<Character> getLetter(@PathVariable Long gameId) {
        char currentLetter = roundService.getCurrentRoundLetter(gameId);
        if (currentLetter != '\0') {
            return new ResponseEntity<>(currentLetter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    // Helper DTO
    public static class PlayerAnswersUpdateRequest {
        private String username;
        private Map<String, String> categoryAnswers;

        // Getters and setters

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Map<String, String> getCategoryAnswers() {
            return categoryAnswers;
        }

        public void setCategoryAnswers(Map<String, String> categoryAnswers) {
            this.categoryAnswers = categoryAnswers;
        }
    }
}
