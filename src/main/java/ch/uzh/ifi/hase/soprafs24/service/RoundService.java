package ch.uzh.ifi.hase.soprafs24.service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RoundRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus.ONGOING;

@Service
public class RoundService {
    private static final Random random = new Random();
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
        if (game!=null && game.getStatus()== GameStatus.FINISHED){
            if (!game.getRounds().isEmpty()) {
                roundRepository.deleteAll(game.getRounds());
                game.getRounds().clear(); // Clear the list after deleting the rounds
            }
            game.setRoundCount(0);
            game.setGamePoints("");
            gameRepository.save(game);
            game.setStatus(GameStatus.ANSWER);
        }
        if (game == null) {
            game = new Game();
            game.setLobby(lobby);
            lobby.setGame(game);
            gameRepository.save(game);
        }
        lobby.setLobbyStatus(ONGOING);
        Round newRound = new Round();
        newRound.setGame(game);

        // Collect all previously assigned letters plus the excluded chars
        List<Character> allExcludedChars = new ArrayList<>(lobby.getExcludedChars());
        game.getRounds().forEach(round -> allExcludedChars.add(round.getAssignedLetter()));

        char roundLetter = generateRandomLetter(allExcludedChars);
        newRound.setAssignedLetter(roundLetter);

        // Set letter position based on lobby difficulty
        newRound.setLetterPosition(determineLetterPosition(lobby.getGameMode()));

        game.getRounds().add(newRound);  // Add new round to the list of rounds in the game
        lobbyRepository.save(lobby);
        roundRepository.save(newRound);
        gameRepository.save(game);  // Save changes to the game
    }

    private char generateRandomLetter(List<Character> excludedChars) {
        char randomLetter;
        do {
            // Generate a random uppercase letter between 'A' and 'Z'
            randomLetter = (char) ('A' + random.nextInt(26));
        } while (excludedChars.contains(randomLetter)); // Ensure it's not in the excluded list
        return randomLetter;
    }

    private int determineLetterPosition(String difficulty) {
        if (Objects.equals(difficulty, "NORMAL")) {
            return 0; // Always the first position for easy mode
        }
        else {
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
        Optional<Game> currentGameOptional = gameRepository.findById(gameId);
        if (currentGameOptional.isEmpty()) {
            throw new RuntimeException("No current game for game ID: " + gameId);
        }

        Game currentGame = currentGameOptional.get();

        // Assume no round found exception handling here
        Round currentRound = getCurrentRoundByGameId(gameId);
        if (currentRound == null) {
            throw new RuntimeException("No current round found for game ID: " + gameId);
        }

        // Deserialize game points
        String existingGamePointsJson = currentGame.getGamePoints();
        Map<String, Integer> gamePoints;
        if (existingGamePointsJson != null && !existingGamePointsJson.isEmpty()) {
            gamePoints = objectMapper.readValue(existingGamePointsJson, new TypeReference<Map<String, Integer>>() {});
        } else {
            gamePoints = new HashMap<>();
        }

        // Deserialize scores from the current round
        Map<String, Map<String, Map<String, Object>>> scoresAndAnswers = objectMapper.readValue(currentRound.getRoundPoints(),
                new TypeReference<Map<String, Map<String, Map<String, Object>>>>() {});

        // Merge current round scores into game points
        scoresAndAnswers.forEach((category, userScores) -> {
            userScores.forEach((username, details) -> {
                Integer roundScore = (Integer) details.get("score");
                gamePoints.merge(username, roundScore, Integer::sum);
            });
        });

        // Serialize the updated game points and save back to the game entity
        String updatedGamePointsJson = objectMapper.writeValueAsString(gamePoints);
        currentGame.setGamePoints(updatedGamePointsJson);
        gameRepository.save(currentGame);

        // Sort the gamePoints by value in descending order and return
        if (!currentGame.getRounds().isEmpty()) {
            currentGame.getRounds().get(currentGame.getRounds().size() - 1).setScorePerRound(currentGame.getGamePoints());
        }
        roundRepository.save(currentRound);
        return gamePoints.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // if there are duplicates, keep the existing
                        LinkedHashMap::new
                ));
    }

    public void updatePlayerStatsAndCheckVictories(Long gameId, Map<String, Integer> gamePoints) throws Exception {
        String winnerUsername = Collections.max(gamePoints.entrySet(), Map.Entry.comparingByValue()).getKey();
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        if (optionalGame.isEmpty()) { throw new RuntimeException("No game found for ID" + gameId); }
        Game game = optionalGame.get();
        Lobby lobby = game.getLobby();
        gamePoints.forEach((username, points) -> {
            Optional<Player> playerOpt = playerRepository.findByUsername(username);
            playerOpt.ifPresent(player -> {
                player.setTotalPoints(player.getTotalPoints() + points);
                player.setRoundsPlayed(player.getRoundsPlayed() + lobby.getRounds());
                // Calculate the average points per round with rounding
                BigDecimal totalPoints = new BigDecimal(player.getTotalPoints());
                BigDecimal roundsPlayed = new BigDecimal(player.getRoundsPlayed());
                BigDecimal averagePoints = totalPoints.divide(roundsPlayed, 2, RoundingMode.HALF_UP);
                player.setAveragePointsPerRound(averagePoints.doubleValue());

                int newLevel = calculateLevel(player.getTotalPoints());
                player.setLevel(newLevel);
                if (Objects.equals(player.getUsername(), winnerUsername)) {
                    player.setVictories(player.getVictories() + 1);
                }
                playerRepository.save(player);
            });
        game.setGamePoints("");
        gameRepository.save(game);
        });
    }
    public int calculateLevel(int totalPoints) {
        int level = 1;
        while (25 * Math.pow(level, 2) <= totalPoints) {
            level++;
        }
        return level - 1;  // Subtract 1 because level increments once more after the last valid level
    }
    @Transactional
    public Map<String, Map<String, Map<String, Object>>> calculateFinalScores(Long gameId) {
        Round currentRound = getCurrentRoundByGameId(gameId);
        if (currentRound == null) {
            throw new RuntimeException("No current round found for game ID: " + gameId);
        }
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        if (optionalGame.isEmpty()) {
            throw new RuntimeException("No Game found for game id: " + gameId);
        }
        Game game = optionalGame.get();
        Lobby lobby = game.getLobby();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Map<String, Map<String, Object>>> currentScores;
        try {
            currentScores = objectMapper.readValue(currentRound.getRoundPoints(),
                    new TypeReference<Map<String, Map<String, Map<String, Object>>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse round points", e);
        }

        currentScores.forEach((category, users) -> {
            Map<String, List<String>> validAnswers = new HashMap<>(); // Track all valid answers for uniqueness checks

            // First pass: Apply vetoes and count valid answers
            users.forEach((username, scoreDetails) -> {
                int vetoVotes = (int) scoreDetails.get("vetoVotes");
                int score = (int) scoreDetails.get("score");

                // Check if vetoes fulfill the majority vote threshold
                if (vetoVotes >= Math.ceil(lobby.getPlayers().size() / 2.0)) {
                    score = score == 1 ? 0 : 1; // Flip the score from 1 to 0 or vice versa
                }

                // Update the score after veto check
                scoreDetails.put("score", score);

                // Track valid answers for uniqueness calculation
                if (score == 1) { // only consider initially valid answers for uniqueness checks
                    String answer = (String) scoreDetails.get("answer");
                    validAnswers.computeIfAbsent(answer.toLowerCase(), k -> new ArrayList<>()).add(username);
                }
            });

            // Second pass: Calculate points based on answer uniqueness and category validation
            users.forEach((username, scoreDetails) -> {
                int score = (int) scoreDetails.get("score");
                if (score == 1) {
                    String answer = (String) scoreDetails.get("answer");
                    List<String> sameAnswerUsers = validAnswers.get(answer.toLowerCase());

                    if (sameAnswerUsers.size() == 1) {
                        // Check if this is the only valid answer in the category
                        boolean isOnlyValid = validAnswers.values().stream()
                                .allMatch(usersList -> usersList.contains(username) || usersList.isEmpty());

                        score = isOnlyValid ? 15 : 10; // 15 points if it's the only valid answer in the category, 10 otherwise
                    } else if (sameAnswerUsers.size() > 1) {
                        score = 5; // Other identical valid answers exist
                    }
                }
                // Add bonuses
                int bonusVotes = (int) scoreDetails.get("bonusVotes");
                score += bonusVotes * 3; // Each bonus vote adds 3 points

                scoreDetails.put("score", score);
            });
        });

        // Serialize and save the updated scores back to the round
        try {
            String scoresJson = objectMapper.writeValueAsString(currentScores);
            currentRound.setRoundPoints(scoresJson);
            roundRepository.save(currentRound);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize final scores", e);
        }

        return currentScores;
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
                if (!value.isEmpty() && isLetterPositionValid(value, assignedLetter, letterPosition, difficulty) && value.length() > 1) {
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
        if (Objects.equals(difficulty, "NORMAL")) {  // Easy mode
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
            // URL encode the word to handle spaces and special characters
            String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8.toString());

            URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&format=json&titles=" + encodedWord);
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

                // Check if the response contains "missing" tag
                Pattern pattern = Pattern.compile("\"missing\"");
                Matcher matcher = pattern.matcher(response.toString());
                return !matcher.find();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Map<String, Map<String, Object>>> prepareScoreAdjustments(Long gameId, HashMap<String, HashMap<String, HashMap<String, Object>>> adjustments) throws Exception {
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
        }));

        // Count vetoes and bonuses
        // Iterate over currentScores to ensure all players are considered
        currentScores.forEach((category, users) -> {
            users.forEach((username, userScores) -> {
                // Retrieve adjustment details if present; otherwise, use default values
                Map<String, Object> detail = adjustments.getOrDefault(category, new HashMap<>()).getOrDefault(username, new HashMap<>());
                boolean veto = (boolean) detail.getOrDefault("veto", false);
                boolean bonus = (boolean) detail.getOrDefault("bonus", false);

                // Update the veto and bonus counts
                int vetoVotes = (int) userScores.get("vetoVotes");
                int bonusVotes = (int) userScores.get("bonusVotes");
                userScores.put("vetoVotes", veto ? vetoVotes + 1 : vetoVotes);
                userScores.put("bonusVotes", bonus ? bonusVotes + 1 : bonusVotes);
            });
        });


        // Serialize and save the updated scores back to the round without applying scoring
        String scoresJson = objectMapper.writeValueAsString(currentScores);
        currentRound.setRoundPoints(scoresJson);
        roundRepository.save(currentRound);

        return currentScores;
    }
    @Transactional
    public Map<String, Integer> calculateScoreDifference(Long gameId) throws IOException {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found with ID: " + gameId));
        List<Round> rounds = game.getRounds();

        if (rounds.size() < 2) {
            return new HashMap<>();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Integer> scoreDifferences = new HashMap<>();

        // Finding the last two rounds with non-null `scorePerRound`
        Map<String, Integer> lastRoundScores = null;
        Map<String, Integer> secondLastRoundScores = null;

        for (int i = rounds.size() - 1; i >= 0 && (lastRoundScores == null || secondLastRoundScores == null); i--) {
            if (rounds.get(i).getScorePerRound() != null) {
                if (lastRoundScores == null) {
                    lastRoundScores = objectMapper.readValue(
                            rounds.get(i).getScorePerRound(),
                            new TypeReference<Map<String, Integer>>() {}
                    );
                } else if (secondLastRoundScores == null) {
                    secondLastRoundScores = objectMapper.readValue(
                            rounds.get(i).getScorePerRound(),
                            new TypeReference<Map<String, Integer>>() {}
                    );
                }
            }
        }

        if (lastRoundScores != null && secondLastRoundScores != null) {
            // Calculate the score differences
            Map<String, Integer> finalSecondLastRoundScores = secondLastRoundScores;
            lastRoundScores.forEach((player, score) -> {
                Integer previousScore = finalSecondLastRoundScores.getOrDefault(player, 0);
                scoreDifferences.put(player, score - previousScore);
            });
        }

        return scoreDifferences;
    }

}
