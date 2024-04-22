package ch.uzh.ifi.hase.soprafs24.service;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.transaction.Transactional;

import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class RoundService {

    @Autowired
    private RoundRepository roundRepository;
    @Autowired
    private GameRepository gameRepository;  // Assuming you have a GameRepository
    @Autowired
    private LobbyRepository lobbyRepository;

    @Transactional  // Ensures the entire method is run in a single transaction
    public void startNewRound(Long lobbyId) {
        Lobby lobby = lobbyRepository.findById(lobbyId).orElseThrow(() -> new RuntimeException("Lobby not found"));
        Game game = lobby.getGame();
        if (game == null) {
            game = new Game();
            game.setLobby(lobby);
            lobby.setGame(game);
            gameRepository.save(game);
        }

        Round newRound = new Round();
        newRound.setGame(game);
        game.getRounds().add(newRound);  // Add new round to the list of rounds in the game

        roundRepository.save(newRound);
        gameRepository.save(game);  // Save changes to the game
    }


    public char getCurrentRoundLetter(Long gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game != null && !game.getRounds().isEmpty()) {
            List<Round> rounds = game.getRounds();
            Round lastRound = rounds.get(rounds.size() - 1);
            return lastRound.getAssignedLetter();
        }
        return '\0';
    }

    public void updatePlayerAnswers(Map<String, Map<String, String>> playerAnswers) {
        // Get the current round with answers
        Round round = getCurrentRound();

        // Update round answers with player answers
        Map<String, Answer> roundAnswers = round.getRoundAnswers();
        for (Map.Entry<String, Map<String, String>> entry : playerAnswers.entrySet()) {
            String playerId = entry.getKey();
            Map<String, String> answers = entry.getValue();

            // Check if the player's answers map exists
            Map<String, CategoryAnswer> playerAnswersMap = (Map<String, CategoryAnswer>) roundAnswers.get(playerId);
            if (playerAnswersMap != null) {
                // Update answers for each category
                for (Map.Entry<String, String> answerEntry : answers.entrySet()) {
                    String category = answerEntry.getKey();
                    String answer = answerEntry.getValue();

                    // Check if the category answer exists
                    CategoryAnswer categoryAnswer = playerAnswersMap.get(category);
                    if (categoryAnswer != null) {
                        // Update the answer field
                        categoryAnswer.setAnswer(answer);
                    } else {
                        // Handle the case where the category answer is not found
                        String errorMessage = String.format("Category answer not found for category: %s", category);
                        // You can log the error message or throw an exception if needed
                    }
                }
            } else {
                // Handle the case where the player's answers map is not found
                String errorMessage = String.format("Player answers map not found for player: %s", playerId);
                // You can log the error message or throw an exception if needed
            }
        }

        // Save the updated round
        roundRepository.save(round);
    }

    public void updateAllAnswers(Map<String, Answer> allAnswers) {
        // Get the current round with answers
        Round round = getCurrentRound();

        // Update round answers with all answers
        round.setRoundAnswers(allAnswers);

        // Save the updated round
        roundRepository.save(round);
    }
    public Round getCurrentRound() {
        return roundRepository.findTopByOrderByIdDesc().orElse(null);
    }
    public List<Round> getRoundByGameId(Long gameId) {
        return roundRepository.findByGameId(gameId);
    }
    public Round getCurrentRoundByGameId(Long gameId) {
        return roundRepository.findTopByGameIdOrderByIdDesc(gameId);
    }
    // Add a new method to retrieve all answers for a specific round
    public void saveRound(Round round) {
        roundRepository.save(round);
    }
}
