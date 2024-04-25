package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void whenCreatePlayer_withValidDetails_shouldSavePlayer() {
        when(playerRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        Player mockPlayer = new Player();
        mockPlayer.setUsername("newUser");
        mockPlayer.setPassword("password123");
        when(playerRepository.save(any(Player.class))).thenReturn(mockPlayer);

        // Execute
        Player result = playerService.createPlayer("newUser", "password123");

        // Verify
        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void whenCreatePlayer_withExistingUsername_shouldThrowConflict() {
        when(playerRepository.findByUsername("existingUser")).thenReturn(Optional.of(new Player()));

        // Execute and verify
        assertThrows(ResponseStatusException.class, () -> playerService.createPlayer("existingUser", "password123"),
                "Expected createPlayer to throw, but it didn't");
    }

    @Test
    void whenUpdatePlayer_existingUser_shouldUpdateFields() {
        Player existingPlayer = new Player();
        existingPlayer.setPassword("oldPass");
        existingPlayer.setReady(false);
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(existingPlayer));

        Player updatedPlayer = new Player();
        updatedPlayer.setPassword("newPass");
        updatedPlayer.setReady(true);

        when(playerRepository.save(existingPlayer)).thenReturn(existingPlayer);

        // Execute
        Player result = playerService.updatePlayer("testUser", updatedPlayer);

        // Verify
        assertTrue(result.getReady());
        assertEquals("newPass", result.getPassword());
        verify(playerRepository).save(existingPlayer);
    }

    @Test
    void whenUpdatePlayer_nonExistingUser_shouldThrowNotFound() {
        when(playerRepository.findByUsername("nonUser")).thenReturn(Optional.empty());

        // Execute and verify
        assertThrows(ResponseStatusException.class, () -> playerService.updatePlayer("nonUser", new Player()),
                "Expected updatePlayer to throw, but it didn't");
    }
}
