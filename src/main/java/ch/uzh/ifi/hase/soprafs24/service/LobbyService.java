package ch.uzh.ifi.hase.soprafs24.service; // Correct package statement

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LobbyService {

    @Autowired
    private LobbyRepository lobbyRepository; // Corrected to instance call
    @Autowired
    private GameRepository gameRepository; // Corrected to instance call
    @Autowired
    private PlayerRepository playerRepository; // Added this field

    @Transactional
    public void checkAndStartGame(Lobby lobby) {
        if (areAllPlayersReady(lobby)) {
            Game game = lobby.getGame(); // This assumes the Lobby entity has a getGame() method
            if (game != null && game.getStatus() != GameStatus.ANSWER) {
                game.setStatus(GameStatus.ANSWER);
                gameRepository.save(game);
            }
        }
    }

    private boolean areAllPlayersReady(Lobby lobby) {
        return lobby.getPlayers().stream().allMatch(Player::getReady);
    }
}
