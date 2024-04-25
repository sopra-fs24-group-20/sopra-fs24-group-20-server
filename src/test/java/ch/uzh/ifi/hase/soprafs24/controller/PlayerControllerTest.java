package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
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
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerController playerController;

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
        PlayerPutDTO playerPutDTO = new PlayerPutDTO();
        playerPutDTO.setReady(true);

        Player player = new Player();
        player.setUsername("testUser");
        player.setReady(true);

        when(playerService.updatePlayer(eq("testUser"), any())).thenReturn(player);

        PlayerGetDTO result = playerController.updatePlayer("testUser", playerPutDTO);

        assertNotNull(result);
        assertTrue(result.getReady());
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
