package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test") // Use application-test.yml configuration
public class PlayerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        // Preload some data or set initial conditions
        Player player = new Player();
        player.setUsername("testUser");
        player.setPassword("password123");
        player.setReady(true);
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
    }

    @Test
    void whenFindByUsername_withNoResult_thenReturnEmpty() {
        // When
        Optional<Player> notFoundPlayer = playerRepository.findByUsername("nonExistentUser");

        // Then
        assertThat(notFoundPlayer.isPresent()).isFalse();
    }
}
