package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class LobbyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LobbyRepository lobbyRepository;

    @Test
    public void whenFindByLobbyName_thenReturnLobby() {
        // given
        Lobby lobby = new Lobby();
        lobby.setLobbyName("TestLobby");
        entityManager.persist(lobby);
        entityManager.flush();

        // when
        Optional<Lobby> found = lobbyRepository.findByLobbyName(lobby.getLobbyName());

        // then
        assertTrue(found.isPresent());
        assertEquals(found.get().getLobbyName(), lobby.getLobbyName());
    }

    @Test
    public void whenFindByLobbyName_thenReturnEmpty() {
        // when
        Optional<Lobby> found = lobbyRepository.findByLobbyName("NonExistent");

        // then
        assertFalse(found.isPresent());
    }
    @Test
    public void whenFindById_thenReturnLobby() {
        // given
        Lobby lobby = new Lobby();
        lobby.setLobbyName("TestLobby");
        entityManager.persist(lobby);
        entityManager.flush();

        // when
        Optional<Lobby> found = lobbyRepository.findById(lobby.getLobbyId());

        // then
        assertTrue(found.isPresent());
        assertEquals(found.get().getLobbyName(), lobby.getLobbyName());
    }

    @Test
    public void whenFindById_thenReturnEmpty() {

        // when
        Optional<Lobby> found = lobbyRepository.findById(1L);

        // then
        assertFalse(found.isPresent());
    }
}
