package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
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
    /*
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
    public ResponseEntity<Void> putPlayerAnswers(@RequestBody PlayerAnswersRequest playerAnswersRequest) {
        try {
            // Extract data from the request
            String roundId = playerAnswersRequest.getRoundId();
            String username = playerAnswersRequest.getUsername();
            Map<String, String> categoryAnswers = playerAnswersRequest.getAnswers();

            // Call the existing method to update player answers
            roundService.updatePlayerAnswers(roundId, username, categoryAnswers);

            // Return a success response
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // Handle any exceptions and return appropriate response
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/answers/all")
    public ResponseEntity<?> updateAnswers(@PathVariable Long roundId, @RequestBody Map<String, List<Round.PlayerCategoryResponse>> newPlayerAnswers) {
        roundService.updateAllAnswers(roundId, newPlayerAnswers);
        return ResponseEntity.ok("Answers updated successfully");
    }
     */
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
