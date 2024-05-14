package ch.uzh.ifi.hase.soprafs24.service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
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
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerRepository playerRepository;

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
        char roundLetter = generateRandomLetter(lobby.getExcludedChars());
        newRound.setAssignedLetter(roundLetter);

        // Set letter position based on lobby difficulty
        newRound.setLetterPosition(determineLetterPosition(lobby.getGameMode()));

        game.getRounds().add(newRound);  // Add new round to the list of rounds in the game
        roundRepository.save(newRound);
        gameRepository.save(game);  // Save changes to the game
    }

    private int determineLetterPosition(String difficulty) {
        if (Objects.equals(difficulty, "0")) {
            return 0; // Always the first position for easy mode
        }
        else {
            Random random = new Random();
            int[] possiblePositions = {0, 1, 2, -1}; // Last position is denoted by -1
            return possiblePositions[random.nextInt(possiblePositions.length)];
        }
    }
    public int getCurrentRoundLetterPosition(Long gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game != null && !game.getRounds().isEmpty()) {
            List<Round> rounds = game.getRounds();
            Round lastRound = rounds.get(rounds.size() - 1);
            return lastRound.getLetterPosition();
        }
        return -100; // Indicates that no valid round or game was found, distinct from -1 which is a valid position
    }

    private char generateRandomLetter(List<Character> excludedChars) {
        Random random = new Random();
        char randomLetter;
        do {
            // Generate a random uppercase letter between 'A' and 'Z'
            randomLetter = (char) ('A' + random.nextInt(26));
        } while (excludedChars.contains(randomLetter)); // Check against excluded characters
        return randomLetter;
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
    public Round getCurrentRound(Long gameId) {
        return roundRepository.findTopByGameIdOrderByIdDesc(gameId).orElse(null);
    }
    public List<Round> getRoundByGameId(Long gameId) {
        return roundRepository.findByGameId(gameId);
    }
    public Round getCurrentRoundByGameId(Long gameId) {
        return roundRepository.findTopByGameIdOrderByIdDesc(gameId).orElse(null);
    }
    public void saveRound(Round round) {
        roundRepository.save(round);
    }
    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

    //sum up the scores from the function below
    @Transactional
    public Map<String, Integer> calculateLeaderboard(Long gameId) throws Exception {
        Round currentRound = getCurrentRoundByGameId(gameId);
        Optional<Game> currentGameOptional = gameRepository.findById(gameId);
        if (currentGameOptional.isEmpty()) {
            throw new RuntimeException("No current game for game ID: " + gameId);
        }

        Game currentGame = currentGameOptional.get();  // Get the Game object from Optional
        Lobby lobby = currentGame.getLobby();
        int totalRounds = lobby.getRounds();

        if (currentRound == null) {
            throw new RuntimeException("No current round found for game ID: " + gameId);
        }

        // Reload the round to ensure it reflects the latest changes

        // Now, fetch the scores
        Map<String, Map<String, Map<String, Object>>> scoresAndAnswers = objectMapper.readValue(currentRound.getRoundPoints(),
                new TypeReference<Map<String, Map<String, Map<String, Object>>>>() {});

        Map<String, Integer> finalScores = new HashMap<>();
        Map<String, Player> playersToUpdate = new HashMap<>();

        // Aggregate scores
        scoresAndAnswers.forEach((category, userScores) -> {
            userScores.forEach((username, details) -> {
                Integer score = (Integer) details.get("score");
                finalScores.merge(username, score, Integer::sum);

                // Fetch the player (mock method, replace with actual)
                Player player = playerService.getPlayerByUsername(username);
                if (player != null) {
                    playersToUpdate.putIfAbsent(username, player);
                    player.setTotalPoints(player.getTotalPoints() + score);

                    // Note: We are not updating roundsPlayed or calculating the average here
                }
            });
        });

        // Now, update roundsPlayed and calculate the average points per round for each player
        playersToUpdate.values().forEach(player -> {
            player.setRoundsPlayed(player.getRoundsPlayed() + 1);  // Increment rounds first
            if (player.getRoundsPlayed() > 0) {
                double average = (double) player.getTotalPoints() / player.getRoundsPlayed();
                double roundedAverage = Math.round(average * 100) / 100.0;
                player.setAveragePointsPerRound(roundedAverage);
            } else {
                player.setAveragePointsPerRound(0.0); // Just in case, but this case should logically not happen here
            }
            savePlayer(player);  // Save the player with updated stats
        });

        // Check if this is the final round
        if (currentGame.getRounds().size() == totalRounds) {
            // This is the final round
            final String winner = finalScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (winner != null) {
                Player winningPlayer = playerService.getPlayerByUsername(winner);
                if (winningPlayer != null) {
                    winningPlayer.setVictories(winningPlayer.getVictories() + 1);
                    savePlayer(winningPlayer);  // Ensure victory is recorded
                }
            }
        }
        // Return sorted scores
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

        boolean autoCorrectEnabled = currentRound.getGame().getLobby().getAutoCorrectMode() != null && currentRound.getGame().getLobby().getAutoCorrectMode();
        String difficulty = currentRound.getGame().getLobby().getGameMode();
        char assignedLetter = currentRound.getAssignedLetter();
        int letterPosition = currentRound.getLetterPosition();  // Assumed to be set in the round
        String answersJson = currentRound.getPlayerAnswers();

        if (answersJson == null || answersJson.isEmpty()) {
            return new HashMap<>();
        }

        List<Map<String, String>> answers = objectMapper.readValue(
                "[" + answersJson + "]", new TypeReference<List<Map<String, String>>>() {});

        Map<String, Map<String, String>> answersByCategory = new HashMap<>();
        answers.forEach(answer -> answer.keySet().forEach(key -> {
            if (!key.equals("username")) {
                String value = answer.getOrDefault(key, "");
                answersByCategory.computeIfAbsent(key, k -> new HashMap<>())
                        .put(answer.get("username"), value);
            }
        }));

        Map<String, Map<String, Map<String, Object>>> categoryScores = new HashMap<>();
        answersByCategory.forEach((category, userAnswers) -> {
            Map<String, Map<String, Object>> userScoresAndAnswers = new HashMap<>();
            userAnswers.forEach((username, value) -> {
                int points = 0;
                if (!value.isEmpty() && isLetterPositionValid(value, assignedLetter, letterPosition, difficulty)) {
                    if (!autoCorrectEnabled || checkWordExists(value)) {
                        points = 1;  // Word is valid
                    }
                }

                Map<String, Object> scoreAndAnswer = new HashMap<>();
                scoreAndAnswer.put("score", points);
                scoreAndAnswer.put("answer", value);
                userScoresAndAnswers.put(username, scoreAndAnswer);
            });

            categoryScores.put(category, userScoresAndAnswers);
        });

        ObjectMapper objectMapper = new ObjectMapper();
        String scoresJson = objectMapper.writeValueAsString(categoryScores);
        if (currentRound.getRoundPoints() == null || currentRound.getRoundPoints().isEmpty()) {
            currentRound.setRoundPoints(scoresJson);
            roundRepository.save(currentRound);
        }
        return categoryScores;
    }

    private boolean isLetterPositionValid(String word, char assignedLetter, int letterPosition, String difficulty) {
        if (Objects.equals(difficulty, "0")) {  // Easy mode
            return word.toLowerCase().charAt(0) == Character.toLowerCase(assignedLetter);
        }
        else {  // Normal mode
            if (letterPosition == -1) {  // Last character check
                return word.toLowerCase().charAt(word.length() - 1) == Character.toLowerCase(assignedLetter);
            } else {
                return word.length() > letterPosition && word.toLowerCase().charAt(letterPosition) == Character.toLowerCase(assignedLetter);
            }
        }
    }

    public boolean checkWordExists(String word) {
        try {
            URL url = new URL("https://en.wiktionary.org/w/api.php?action=query&format=json&titles=" + word);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();


                Pattern pattern = Pattern.compile("\"missing\"\\s*:\\s*\"\"");
                Matcher matcher = pattern.matcher(response.toString());
                return !matcher.find();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Map<String, Map<String, Object>>> adjustScores(Long gameId, HashMap<String, HashMap<String, HashMap<String, Object>>> adjustments) throws Exception {
        Round currentRound = getCurrentRoundByGameId(gameId);
        if (currentRound == null) {
            throw new RuntimeException("No current round found for game ID: " + gameId);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String answersJson = currentRound.getRoundPoints().toString();
        TypeReference<Map<String, Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {};
        Map<String, Map<String, Map<String, Object>>> currentScores = objectMapper.readValue(answersJson, typeRef);

        // Initialize the structure for counting votes
        currentScores.forEach((category, users) -> users.forEach((username, userScores) -> {
            userScores.putIfAbsent("vetoVotes", 0);
            userScores.putIfAbsent("bonusVotes", 0);
            userScores.putIfAbsent("submissionsCount", 0);
        }));

        // Count vetoes and bonuses
        adjustments.forEach((category, users) -> users.forEach((username, details) -> {
            boolean veto = (boolean) details.get("veto");
            boolean bonus = (boolean) details.get("bonus");

            Map<String, Object> userScores = currentScores.get(category).get(username);
            if (userScores != null) {
                int vetoVotes = (int) userScores.get("vetoVotes");
                int bonusVotes = (int) userScores.get("bonusVotes");
                int submissionsCount = (int) userScores.get("submissionsCount");

                // Update counts based on the current adjustments
                if (veto) {
                    userScores.put("vetoVotes", vetoVotes + 1);
                }
                if (bonus) {
                    userScores.put("bonusVotes", bonusVotes + 1);
                }
                userScores.put("submissionsCount", submissionsCount + 1);
            }
        }));

        // Serialize and save the updated scores back to the round without applying scoring
        String scoresJson = objectMapper.writeValueAsString(currentScores);
        currentRound.setRoundPoints(scoresJson);
        roundRepository.save(currentRound);

        return currentScores;
    }

}
