package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    @Test
    void leaveLobby_WhenPlayerIsNotOwner_ShouldLeaveSuccessfully() {
        Long lobbyId = 1L;
        Player owner = new Player();
        owner.setUsername("ownerUser");
        Player member = new Player();
        member.setUsername("memberUser");

        // Use ArrayList to ensure the list is mutable
        Lobby lobby = new Lobby();
        lobby.setLobbyOwner(owner);
        lobby.setPlayers(new ArrayList<>(Arrays.asList(owner, member)));

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));

        // Act
        boolean result = lobbyService.leaveLobby(lobbyId, member.getUsername());

        // Assert
        assertTrue(result);
        assertFalse(lobby.getPlayers().contains(member));
        verify(lobbyRepository).save(lobby); // This confirms the repository's save method is called, implying updates are handled.
    }

    @Test
    void leaveLobby_WhenOwnerLeavesAndIsOnlyPlayer_ShouldDeleteLobby() {
        Long lobbyId = 1L;
        Player owner = new Player();
        owner.setUsername("ownerUser");

        // Use a mutable list to prevent UnsupportedOperationException
        Lobby lobby = new Lobby();
        lobby.setLobbyOwner(owner);
        lobby.setPlayers(new ArrayList<>(Arrays.asList(owner))); // Wrapping with ArrayList for mutability

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));

        boolean result = lobbyService.leaveLobby(lobbyId, owner.getUsername());

        assertTrue(result);
        verify(lobbyRepository).delete(lobby); // This will check if the delete method is correctly invoked
    }

    @Test
    void leaveLobby_WhenOwnerLeavesAndOthersRemain_ShouldTransferOwnership() {
        Long lobbyId = 1L;
        Player owner = new Player();
        owner.setUsername("ownerUser");
        Player newOwner = new Player();
        newOwner.setUsername("newOwnerUser");

        // Use a mutable ArrayList to avoid UnsupportedOperationException
        List<Player> players = new ArrayList<>();
        players.add(owner);
        players.add(newOwner);

        Lobby lobby = new Lobby();
        lobby.setLobbyOwner(owner);
        lobby.setPlayers(players);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));

        boolean result = lobbyService.leaveLobby(lobbyId, owner.getUsername());

        assertTrue(result);
        assertEquals(newOwner, lobby.getLobbyOwner());
        verify(lobbyRepository).save(lobby);
    }



    @Test
    void leaveLobby_WhenLobbyDoesNotExist_ShouldThrowException() {
        // Arrange
        Long lobbyId = 1L;
        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            lobbyService.leaveLobby(lobbyId, "anyUser");
        });

        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatus());
    }
}
