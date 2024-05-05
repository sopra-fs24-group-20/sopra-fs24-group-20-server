package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class PlayerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        // Create and persist a Lobby entity
        Lobby lobby = new Lobby();
        Game game = new Game();
        Round round = new Round();
        // Additional Lobby setup, if needed

        // Preload some data or set initial conditions
        Player player = new Player();
        player.setUsername("testUser");
        player.setPassword("password123");
        player.setReady(true);
        player.setLobby(lobby);  // Associate the player with the lobby
        entityManager.persist(player);
        entityManager.flush();
    }

    @Test
    void whenFindByUsername_thenReturnPlayer() {
        // When
        Optional<Player> foundPlayer = playerRepository.findByUsername("testUser");

        // Then
        assertThat(foundPlayer.isPresent()).isTrue();
        assertThat(foundPlayer.get().getUsername()).isEqualTo("testUser");
        assertThat(foundPlayer.get().getLobby()).isNotNull();  // Verify the lobby association
    }

    @Test
    void whenFindByUsername_withNoResult_thenReturnEmpty() {
        // When
        Optional<Player> notFoundPlayer = playerRepository.findByUsername("nonExistentUser");

        // Then
        assertThat(notFoundPlayer.isPresent()).isFalse();
    }
}
