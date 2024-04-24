package ch.uzh.ifi.hase.soprafs24.service;
import java.util.*;

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

    @Transactional
    public void updatePlayerAnswers(Long gameId, String username, Map<String, String> categoryAnswers) {
        // Retrieve the round based on ID, throwing an exception if not found
        Round round = getCurrentRoundByGameId(gameId);

        // Get the existing answers map or initialize a new one if none exists for the user
        Map<String, List<Round.PlayerCategoryResponse>> playerAnswers = round.getRoundAnswers();
        List<Round.PlayerCategoryResponse> answers = playerAnswers.getOrDefault(username, new ArrayList<>());

        // Iterate through each category-answer pair provided
        categoryAnswers.forEach((category, answer) -> {
            // Check if an answer for the category already exists
            Optional<Round.PlayerCategoryResponse> existingResponse = answers.stream()
                    .filter(r -> r.getCategory().equals(category))
                    .findFirst();

            if (existingResponse.isPresent()) {
                // If it exists, update the existing answer
                existingResponse.get().getCategoryAnswer().setAnswer(answer);
            } else {
                // If not, add a new answer with 0 points
                answers.add(new Round.PlayerCategoryResponse(category, new CategoryAnswer(answer, 0)));
            }
        });

        // Update the map with the new list of answers for the user
        playerAnswers.put(username, answers);
        // Save the updated round
        roundRepository.save(round);
    }




    @Transactional
    public void updateAllAnswers(Long GameId, Map<String, List<Round.PlayerCategoryResponse>> newPlayerAnswers) {
        // Retrieve the round based on ID
        Round round = getCurrentRoundByGameId(GameId);

        // Replace the existing answers map with the new one
        round.setRoundAnswers(newPlayerAnswers);

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
