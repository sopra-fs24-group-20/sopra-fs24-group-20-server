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
    @Test
    void logoutPlayer_WithGuestUsername_ShouldDeletePlayer() {
        // Arrange
        String username = "Guest:12345";
        Player mockPlayer = new Player();
        mockPlayer.setUsername(username);

        when(playerRepository.findByUsername(username)).thenReturn(Optional.of(mockPlayer));
        doNothing().when(playerRepository).delete(mockPlayer);

        // Act
        Player result = playerService.LogOutPlayer(username);

        // Assert
        assertNull(result, "Player should be deleted and return null");
        verify(playerRepository).delete(mockPlayer); // Verify that the player was deleted
    }

    @Test
    void logoutPlayer_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        String username = "nonexistentUser";
        when(playerRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.LogOutPlayer(username);
        });

        assertEquals("Player not found with username: " + username, exception.getMessage());
    }
    @Test
    void getPlayerByUsername_ShouldReturnPlayer() {
        // Arrange
        String username = "existingUser";
        Player expectedPlayer = new Player();
        expectedPlayer.setUsername(username);

        when(playerRepository.findByUsername(username)).thenReturn(Optional.of(expectedPlayer));

        // Act
        Player actualPlayer = playerService.getPlayerByUsername(username);

        // Assert
        assertNotNull(actualPlayer, "Player should not be null");
        assertEquals(expectedPlayer.getUsername(), actualPlayer.getUsername(), "Returned player username should match the request");
    }

    @Test
    void getPlayerByUsername_ShouldThrowNotFoundException() {
        // Arrange
        String username = "nonexistentUser";
        when(playerRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            playerService.getPlayerByUsername(username);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getReason().contains(username));
    }

    @Test
    void logInPlayer_ShouldReturnPlayerWhenCredentialsMatch() {
        // Arrange
        String username = "validUser";
        String password = "validPassword";
        Player validPlayer = new Player();
        validPlayer.setUsername(username);
        validPlayer.setPassword(password);

        when(playerRepository.findByUsername(username)).thenReturn(Optional.of(validPlayer));

        // Act
        Player loggedInPlayer = playerService.LogInPlayer(username, password);

        // Assert
        assertNotNull(loggedInPlayer, "Logged in player should not be null");
        assertEquals(username, loggedInPlayer.getUsername(), "Logged in player should have the correct username");
    }

    @Test
    void logInPlayer_ShouldThrowUnauthorizedExceptionWhenCredentialsDoNotMatch() {
        // Arrange
        String username = "user";
        String password = "wrongPassword";
        Player validPlayer = new Player();
        validPlayer.setUsername(username);
        validPlayer.setPassword("correctPassword");

        when(playerRepository.findByUsername(username)).thenReturn(Optional.of(validPlayer));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            playerService.LogInPlayer(username, password);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertTrue(exception.getReason().contains("Username or Password Incorrect"));
    }

    @Test
    void logInPlayer_ShouldThrowUnauthorizedExceptionWhenUserNotFound() {
        // Arrange
        String username = "nonexistentUser";
        String password = "anyPassword";
        when(playerRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            playerService.LogInPlayer(username, password);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertTrue(exception.getReason().contains("Username or Password Incorrect"));
    }
}
