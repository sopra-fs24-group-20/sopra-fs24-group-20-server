package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/*
@DataJpaTest
public class UserRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private PlayerRepository userRepository;

  @Test
  public void findByName_success() {
    // given
    Player player = new Player();
    player.setUsername("firstname@lastname");
    player.setReady(false);
    player.setToken("1");

    entityManager.persist(player);
    entityManager.flush();

    // when
    Optional<Player> found = userRepository.findByUsername(player.getUsername());

    // then
    assertEquals(found.getUsername(), player.getUsername());
    assertEquals(found.getToken(), player.getToken());
    assertEquals(found.getReady(), player.getReady());
  }
}
*/