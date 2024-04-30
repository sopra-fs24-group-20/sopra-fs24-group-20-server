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
        char roundLetter = generateRandomLetter(lobby.getExcludedChars());
        newRound.setAssignedLetter(roundLetter);
        game.getRounds().add(newRound);  // Add new round to the list of rounds in the game

        roundRepository.save(newRound);
        gameRepository.save(game);  // Save changes to the game
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

    //sum up the scores from the function below
    @Transactional
    public Map<String, Integer> calculateLeaderboard(Long gameId) throws Exception {
        Round currentRound = getCurrentRoundByGameId(gameId);
        if (currentRound == null) {
            throw new RuntimeException("No current round found for game ID: " + gameId);
        }

        // Reload the round to ensure it reflects the latest changes

        // Now, fetch the scores
        Map<String, Map<String, Map<String, Object>>> scoresAndAnswers = objectMapper.readValue(currentRound.getRoundPoints(),
                new TypeReference<Map<String, Map<String, Map<String, Object>>>>() {});

        Map<String, Integer> finalScores = new HashMap<>();

        // Aggregate scores
        scoresAndAnswers.forEach((category, userScores) -> {
            userScores.forEach((username, details) -> {
                Integer score = (Integer) details.get("score");
                finalScores.merge(username, score, Integer::sum);
            });
        });

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
        // get the assigned letter of the round
        char assignedLetter = currentRound.getAssignedLetter();
        String answersJson = currentRound.getPlayerAnswers();
        if (answersJson == null || answersJson.isEmpty()) {
            return new HashMap<>();
        }


        List<Map<String, String>> answers = objectMapper.readValue(
                "[" + answersJson + "]", new TypeReference<List<Map<String, String>>>() {});

        Map<String, Map<String, String>> answersByCategory = new HashMap<>();

        // Collect and parse jsoen
        answers.forEach(answer -> answer.keySet().forEach(key -> {
            if (!key.equals("username")) {
                String value = answer.getOrDefault(key, "");
                answersByCategory.computeIfAbsent(key, k -> new HashMap<>())
                        .put(answer.get("username"), value);
            }
        }));

        Map<String, Map<String, Map<String, Object>>> categoryScores = new HashMap<>();
        answersByCategory.forEach((category, userAnswers) -> {
            Map<String, Set<String>> uniqueCheck = new HashMap<>();

            // Check answer
            userAnswers.forEach((username, value) -> {
                if (!value.isEmpty() && value.toLowerCase().charAt(0) == Character.toLowerCase(assignedLetter)) {
                    uniqueCheck.computeIfAbsent(value.toLowerCase(), v -> new HashSet<>()).add(username);
                }
            });

            Map<String, Map<String, Object>> userScoresAndAnswers = new HashMap<>();
            userAnswers.forEach((username, value) -> {
                int points = 0;
                //if word beginns with right letter
                if (!value.isEmpty() && value.toLowerCase().charAt(0) == Character.toLowerCase(assignedLetter)) {
                    if (checkWordExists(value)) {
                        if (uniqueCheck.get(value.toLowerCase()).size() == 1) {
                            points = 10;//word unique
                        } else {
                            points = 5;//word duplicated
                        }
                    } else {//word not exist
                        points = 0;
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
        if(currentRound.getRoundPoints()==null||currentRound.getRoundPoints().isEmpty()){
        currentRound.setRoundPoints(scoresJson);
        roundRepository.save(currentRound);}
        return categoryScores;
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

        // Retrieve the current scores stored in the Round

        ObjectMapper objectMapper = new ObjectMapper();
        String answersJson = currentRound.getRoundPoints().toString();
        TypeReference<Map<String, Map<String, Map<String, Object>>>> typeRef = new TypeReference<>() {};
        Map<String, Map<String, Map<String, Object>>> currentScores = objectMapper.readValue(answersJson, typeRef);

        // Prepare to aggregate vetoes and bonuses
        Map<String, Map<String, Boolean>> hasVeto = new HashMap<>();
        Map<String, Map<String, Integer>> bonusCounts = new HashMap<>();

        // Count vetoes and bonuses
        adjustments.forEach((category, users) -> users.forEach((username, details) -> {
            boolean veto = (boolean) details.get("veto");
            boolean bonus = (boolean) details.get("bonus");
            int bonusCount = bonus ? 3 : 0;  // Each bonus adds 3 points

            hasVeto.computeIfAbsent(category, k -> new HashMap<>()).merge(username, veto, (a, b) -> a || b);
            bonusCounts.computeIfAbsent(category, k -> new HashMap<>()).merge(username, bonusCount, Integer::sum);
        }));

        // Apply vetoes and bonuses
        currentScores.forEach((category, users) -> users.forEach((username, userScores) -> {
            int currentScore = (int) userScores.get("score");

            // Apply vetoes if any
            Boolean veto = hasVeto.getOrDefault(category, new HashMap<>()).get(username);
            if (veto != null && veto) {
                currentScore = (currentScore == 10 ? 0 : 10);  // Flips score if veto is applied
            }

            // Apply bonuses
            Integer bonusAddition = bonusCounts.getOrDefault(category, new HashMap<>()).get(username);
            if (bonusAddition != null) {
                currentScore += bonusAddition;
            }

            userScores.put("score", currentScore);
        }));

        // Serialize and save the updated scores back to the round
        String scoresJson = objectMapper.writeValueAsString(currentScores);
        currentRound.setRoundPoints(scoresJson);
        roundRepository.save(currentRound);
        currentRound.setPlayerAnswers(scoresJson);  // Assuming you need to update this field as well

        return currentScores;
    }
    }

