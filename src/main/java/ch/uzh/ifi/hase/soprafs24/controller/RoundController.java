package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Answer;
import ch.uzh.ifi.hase.soprafs24.entity.CategoryAnswer;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs24.service.RoundService;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Map<String, Answer>> getAllRoundAnswers(@RequestParam Long gameId) {
        List<Round> rounds = roundService.getRoundByGameId(gameId);
        if (rounds != null && !rounds.isEmpty()) {
            // Retrieve the answers for the first round (assuming only one round per game for simplicity)
            Round round = rounds.get(0);
            Map<String, Answer> roundAnswers = round.getRoundAnswers();

            // Return the round answers
            return ResponseEntity.ok(roundAnswers);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping("/answers/player")
    public ResponseEntity<Void> putPlayerAnswers(@RequestBody Map<String, Map<String, String>> playerAnswersFromFrontend) {
        // Process and update the existing roundAnswers map with the player answers received from the frontend
        roundService.updatePlayerAnswers(playerAnswersFromFrontend);

        // Return a success response
        return ResponseEntity.ok().build();
    }

    @PutMapping("/answers/all")
    public ResponseEntity<Void> putAllAnswers(@RequestBody Map<String, Answer> roundAnswersFromFrontend) {
        // Update the existing roundAnswers map with the parsed round answers received from the frontend
        roundService.updateAllAnswers(roundAnswersFromFrontend);

        // Return a success response
        return ResponseEntity.ok().build();
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
}
