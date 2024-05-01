package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerController playerController;

    @Mock
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getAllPlayers_ShouldReturnAllPlayers() {
        Player player = new Player();
        player.setUsername("testUser");
        player.setPassword("password123");
        player.setReady(true); // Ensure ready is initialized
        List<Player> players = Arrays.asList(player);

        when(playerService.getPlayers()).thenReturn(players);
        List<PlayerGetDTO> playerGetDTOs = playerController.getAllPlayers();

        assertNotNull(playerGetDTOs);
        assertEquals(1, playerGetDTOs.size());
        assertEquals("testUser", playerGetDTOs.get(0).getUsername());
        assertTrue(playerGetDTOs.get(0).getReady());
    }

    @Test
    void updatePlayer_ShouldUpdateStatus() {
        PlayerPutDTO changes = new PlayerPutDTO();
        changes.setReady(false);

        Player player = new Player();
        player.setUsername("testUser");
        player.setReady(true);

        // Stubbing the updatePlayer method to return the updated player object
        when(playerRepository.findByUsername("testUser")).thenReturn(Optional.of(player));
        // Call the controller method
        ResponseEntity<Void> result = playerController.updatePlayer("testUser", changes);

        // Verify that the result is not null
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        // Assert that the readiness status of the result is true
        assertTrue(player.getReady());
    }

    @Test
    void createPlayer_ShouldCreatePlayer() {
        PlayerPostDTO playerPostDTO = new PlayerPostDTO();
        playerPostDTO.setUsername("newUser");
        playerPostDTO.setPassword("newPassword123");

        Player player = new Player();
        player.setUsername("newUser");
        player.setPassword("newPassword123");
        player.setReady(true); // Initialize ready

        when(playerService.createPlayer(anyString(), anyString())).thenReturn(player);

        PlayerGetDTO result = playerController.createPlayer(playerPostDTO);

        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
        assertTrue(result.getReady());
    }

    @Test
    void getPlayerByUsername_ShouldReturnPlayer() {
        Player player = new Player();
        player.setUsername("existingUser");
        player.setReady(true); // Initialize ready

        when(playerService.getPlayerByUsername("existingUser")).thenReturn(player);

        PlayerGetDTO result = playerController.getPlayerByUsername("existingUser");

        assertNotNull(result);
        assertEquals("existingUser", result.getUsername());
        assertTrue(result.getReady());
    }


    @Test
    void login_ShouldAuthenticateAndReturnPlayer() {
        PlayerPostDTO loginDTO = new PlayerPostDTO();
        loginDTO.setUsername("user");
        loginDTO.setPassword("password");

        Player player = new Player();
        player.setUsername("user");
        player.setPassword("password");
        player.setReady(true); // Initialize ready

        when(playerService.LogInPlayer("user", "password")).thenReturn(player);

        ResponseEntity<PlayerGetDTO> response = playerController.login(loginDTO);

        assertNotNull(response);
        assertEquals("user", response.getBody().getUsername());
        assertTrue(response.getBody().getReady());
    }
}
