package ch.uzh.ifi.hase.soprafs24.service;
import java.util.List;
import java.util.Random;

import javax.transaction.Transactional;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
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
}
