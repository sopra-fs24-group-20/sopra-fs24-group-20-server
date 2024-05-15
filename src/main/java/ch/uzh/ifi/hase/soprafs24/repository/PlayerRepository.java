package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("playerRepository")
public interface PlayerRepository extends JpaRepository<Player, String> {

    Optional<Player> findByUsername(String username);
    Optional<Player> findByLobbyId(Long lobbyId);
    boolean existsByUsername(String username);


}
