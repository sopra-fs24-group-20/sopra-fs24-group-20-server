package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class LobbyServiceTest {
    //SETUP

    @Mock
    private LobbyRepository lobbyRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private LobbyService lobbyService;

    private Lobby lobby;
    private Player owner;

    @BeforeEach
    void setup() {
        owner = new Player();
        owner.setUsername("ownerUser");
        lobby = new Lobby();
        lobby.setLobbyName("TestLobby");
        lobby.setLobbyOwner(owner);
        lobby.setLobbyPassword("pass123");
    }


    // TEST BELOW
    @Test
    void testCreateLobby_ValidInputs() {
        when(lobbyRepository.findByLobbyName(anyString())).thenReturn(Optional.empty());
        when(lobbyRepository.save(any(Lobby.class))).thenReturn(lobby);

        Lobby created = lobbyService.createLobby("TestLobby", "pass123", owner);

        assertNotNull(created);
        assertEquals("TestLobby", created.getLobbyName());
        verify(lobbyRepository).save(any(Lobby.class));
    }

    @Test
    void testCreateLobby_DuplicateLobbyName() {
        when(lobbyRepository.findByLobbyName("TestLobby")).thenReturn(Optional.of(lobby));

        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.createLobby("TestLobby", "pass123", owner);
        });
    }

    @Test
    void testDeleteLobbyById_LobbyExists() {
        when(lobbyRepository.findById(anyLong())).thenReturn(Optional.of(lobby));
        doNothing().when(lobbyRepository).delete(any(Lobby.class));

        lobbyService.deleteLobbyById(1L);

        verify(lobbyRepository).delete(lobby);
    }

    @Test
    void testDeleteLobbyById_LobbyNotFound() {
        when(lobbyRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.deleteLobbyById(1L);
        });
    }


}


