package ch.uzh.ifi.hase.soprafs24.service;
import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Statistic;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public int getPoints(String username) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        // This method would need to calculate the points from the player's stats
        // Example calculation, replace with actual logic
        return player.getStats().stream()
                .flatMapToInt(stats -> stats.getPoints().stream().mapToInt(Integer::intValue))
                .sum();
    }

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public Player createPlayer(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must not be null or empty");
        }
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username must be unique");
        }
        Player player = new Player();
        player.setUsername(username);
        player.setStats(new ArrayList<>());
        player.setPassword(password);
        player.setToken("1234");
        player.setReady(false);

        return playerRepository.save(player);
    }
    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }
    public Player updatePlayer(String username, Player updatedPlayer) {
        return playerRepository.findByUsername(username).map(player -> {
            // Update player's fields here. For example:
            if (updatedPlayer.getPassword() != null) {
                player.setPassword(updatedPlayer.getPassword());
            }
            player.setReady(updatedPlayer.getReady());
            // Add other fields you wish to update.
            //LobbyService.checkAndStartGame();
            return playerRepository.save(player);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found with username: " + username));
    }

    public Player getPlayerByUsername(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found with username: " + username));
    }
    public Player LogInPlayer(String username, String password) {
        return playerRepository.findByUsername(username)
                .filter(player -> password.equals(player.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Password Incorrect"));
    }
    public boolean addAnswer(String username, List<String> answers) {
        // Using Optional to handle possible null value for player
        Optional<Player> optionalPlayer = playerRepository.findByUsername(username);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            //if (player.getLobby().getGame().getStatus() == GameStatus.ANSWER) {
                if(true){
                Statistic stat = new Statistic();
                stat.setAnswer(answers);
                player.getStats().add(stat);
                playerRepository.save(player);
                return true;
            }
        }
        return false;
    }
    public boolean addVotes(String username, List<Boolean> votes) {
        // Using the Optional to safely check if the player exists
        Optional<Player> optionalPlayer = playerRepository.findByUsername(username);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            if (player.getLobby().getGame().getStatus() == GameStatus.VOTE) {
                List<Statistic> statistics = player.getStats();
                if (!statistics.isEmpty()) {
                    Statistic lastStat = statistics.get(statistics.size() - 1);
                    List<Integer> vetoUpdates = votes.stream().map(vote -> vote ? 1 : 0).collect(Collectors.toList());
                    lastStat.setVeto(vetoUpdates);
                    playerRepository.save(player);
                    return true;
                }
            }
        }
        return false;
    }

}