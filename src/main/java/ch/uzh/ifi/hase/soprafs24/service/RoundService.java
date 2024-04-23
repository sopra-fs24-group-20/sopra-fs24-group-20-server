package ch.uzh.ifi.hase.soprafs24.service;
import java.util.*;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Autowired
    private ObjectMapper objectMapper;

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
    public Round getCurrentRound() {
        return roundRepository.findTopByOrderByIdDesc().orElse(null);
    }
    public List<Round> getRoundByGameId(Long gameId) {
        return roundRepository.findByGameId(gameId);
    }
    public Round getCurrentRoundByGameId(Long gameId) {
        return roundRepository.findTopByGameIdOrderByIdDesc(gameId);
    }
    public void saveRound(Round round) {
        roundRepository.save(round);
    }
    public Map<String, Integer> calculateLeaderboard(Long gameId) throws Exception {
        Round currentRound = getCurrentRoundByGameId(gameId);
        if (currentRound == null) {
            throw new RuntimeException("No current round found for game ID: " + gameId);
        }

        // Get scores and answers by category
        Map<String, Map<String, Map<String, Object>>> scoresAndAnswers = calculateScoresCategory(gameId);

        Map<String, Integer> finalScores = new HashMap<>();

        // Sum up scores across all categories
        scoresAndAnswers.forEach((category, userScores) -> {
            userScores.forEach((username, details) -> {
                Integer score = (Integer) details.get("score");
                finalScores.merge(username, score, Integer::sum); // Adds scores for the same username across categories
            });
        });

        // Sorting the leaderboard
        return finalScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }


    public Map<String, Map<String, Map<String, Object>>> calculateScoresCategory(Long gameId) throws Exception {
        Round currentRound = getCurrentRoundByGameId(gameId);
        if (currentRound == null) {
            throw new RuntimeException("No current round found for game ID: " + gameId);
        }

        char assignedLetter = currentRound.getAssignedLetter(); // Fetch the assigned letter of the round
        String answersJson = currentRound.getPlayerAnswers();
        if (answersJson == null || answersJson.isEmpty()) {
            return new HashMap<>();
        }

        List<Map<String, String>> answers = objectMapper.readValue(
                "[" + answersJson + "]", new TypeReference<List<Map<String, String>>>() {});

        Map<String, Map<String, String>> answersByCategory = new HashMap<>();

        // Prepare to collect all answers and compute scores
        answers.forEach(answer -> answer.keySet().forEach(key -> {
            if (!key.equals("username")) {
                String value = answer.getOrDefault(key, "");
                answersByCategory.computeIfAbsent(key, k -> new HashMap<>())
                        .put(answer.get("username"), value);
            }
        }));

        // Calculate scores for each category based on uniqueness and include answers
        Map<String, Map<String, Map<String, Object>>> categoryScores = new HashMap<>();
        answersByCategory.forEach((category, userAnswers) -> {
            Map<String, Set<String>> uniqueCheck = new HashMap<>();

            // Check and score each answer
            userAnswers.forEach((username, value) -> {
                if (!value.isEmpty() && value.toLowerCase().charAt(0) == Character.toLowerCase(assignedLetter)) {
                    uniqueCheck.computeIfAbsent(value.toLowerCase(), v -> new HashSet<>()).add(username);
                }
            });

            Map<String, Map<String, Object>> userScoresAndAnswers = new HashMap<>();
            userAnswers.forEach((username, value) -> {
                int points = 0;
                if (!value.isEmpty() && value.toLowerCase().charAt(0) == Character.toLowerCase(assignedLetter) && uniqueCheck.get(value.toLowerCase()).size() == 1) {
                    points = 10;
                } else if (!value.isEmpty() && value.toLowerCase().charAt(0) == Character.toLowerCase(assignedLetter)) {
                    points = 5;
                }

                Map<String, Object> scoreAndAnswer = new HashMap<>();
                scoreAndAnswer.put("score", points);
                scoreAndAnswer.put("answer", value);
                userScoresAndAnswers.put(username, scoreAndAnswer);
            });

            categoryScores.put(category, userScoresAndAnswers);
        });

        return categoryScores;
    }
}
