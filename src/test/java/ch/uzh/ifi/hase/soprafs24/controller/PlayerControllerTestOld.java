/*package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.service.PlayerService;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerPostDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
public class PlayerControllerTestTEST {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private PlayerService userService;

// POST | /players | 201 CREATED
  @Test
  public void createPlayer_validInput() throws Exception {
    // given
    Player player = new Player();
    player.setUsername("testUsername");
    player.setPassword("testPassword");

    PlayerPostDTO playerPostDTO = new PlayerPostDTO();
    playerPostDTO.setUsername(player.getUsername());
    playerPostDTO.setPassword(player.getPassword());

    given(userService.createPlayer(player.getUsername(),player.getPassword())).willReturn(player);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/players")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(playerPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username", is(player.getUsername())));
  }

// POST | /players | 400 BAD REQUEST
  @Test
  public void createPlayer_invalidInput() throws Exception {
      // given
      PlayerPostDTO playerPostDTO = new PlayerPostDTO();
      playerPostDTO.setUsername("testUsername");
      playerPostDTO.setPassword("testPassword");

      given(userService.createPlayer(anyString(), anyString()))
              .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

      // when/then -> do the request + validate the result
      MockHttpServletRequestBuilder postRequest = post("/players")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(playerPostDTO));

      // then
      mockMvc.perform(postRequest)
              .andExpect(status().isBadRequest());
  }

// POST | /players/login | 200 OK
  @Test
  public void loginPlayer_validInput() throws Exception{
      // given
      Player player = new Player();
      player.setUsername("testUsername");
      player.setPassword("testPassword");

      PlayerPostDTO playerPostDTO = new PlayerPostDTO();
      playerPostDTO.setUsername(player.getUsername());
      playerPostDTO.setPassword(player.getPassword());

      given(userService.LogInPlayer(player.getUsername(),player.getPassword())).willReturn(player);

      // when/then -> do the request + validate the result
      MockHttpServletRequestBuilder postRequest = post("/players/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(playerPostDTO));

      // then
      mockMvc.perform(postRequest)
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.username", is(player.getUsername())));
  }

// POST | /players/login | 401 UNAUTHORISED
  @Test
  public void loginPlayer_invalidInput() throws Exception{
      // given
      PlayerPostDTO playerPostDTO = new PlayerPostDTO();
      playerPostDTO.setUsername("testUsername");
      playerPostDTO.setPassword("testPassword");

      given(userService.createPlayer(anyString(), anyString()))
              .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

      // when/then -> do the request + validate the result
      MockHttpServletRequestBuilder postRequest = post("/players/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(playerPostDTO));

      // then
      mockMvc.perform(postRequest)
              .andExpect(status().isUnauthorized());
  }

// GET | /players/username | 200 OK
  @Test
  public void givenPlayer_whenGetUser_thenReturnJsonArray() throws Exception {
      // given
      Player player = new Player();
      player.setUsername("testUsername");

      // this mocks the UserService -> we define above what the userService should
      given(userService.getPlayerByUsername(player.getUsername())).willReturn(player);

      // when
      MockHttpServletRequestBuilder getRequest = get("/players/{username}", player.getUsername())
              .contentType(MediaType.APPLICATION_JSON);

      // then
      mockMvc.perform(getRequest)
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.username", is(player.getUsername())));
  }

// GET | /players/username | 404 NOT FOUND
  @Test
  public void nonExistingPlayer_whenGetUser_error() throws Exception {
      // given
      Player player = new Player();
      player.setUsername("testUsername");

      // this mocks the UserService -> we define above what the userService should
      given(userService.getPlayerByUsername(anyString()))
              .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
      // when
      MockHttpServletRequestBuilder getRequest = get("/players/{username}", player.getUsername())
              .contentType(MediaType.APPLICATION_JSON);

      // then
      mockMvc.perform(getRequest)
              .andExpect(status().isNotFound());
  }

// PUT | /players/username | 204 NO CONTENT
  /*@Test
  public void setReady_PlayerUpdated() throws Exception {
      // given
      PlayerPostDTO playerPutDTO = new PlayerPostDTO();
      playerPutDTO.setUsername("testUsername");
      playerPutDTO.setPassword("testPassword");

      // Mock userService behavior
      when(userService.getPlayerByUsername(username)).thenReturn(new Player()); // Assume user exists
      doNothing().when(userService).updateUsernameBirthdate(userId, username, LocalDate.parse(birthdate));

      // Create a request body string with username and birthdate
      String requestBody = "{\"username\": \"" + username + "\", \"birthdate\": \"" + birthdate + "\"}";

      // Perform PUT request to /users/{id}
      mockMvc.perform(put("/users/{id}", username)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(requestBody))
              .andExpect(status().isNoContent());

      // Verify that the userService's updateUsernameBirthdate method is called with the correct arguments
      verify(userService).updateUsernameBirthdate(username);
  }

// PUT | /players/username | 404 NOT FOUND
  @Test
  public void setUsernameBirthdate_UserUpdated_Returns204() throws Exception {
      String username = "testUser";
      String birthdate = "2024-03-24";

      // Mock userService behavior
      when(userService.getPlayerByUsername(username)).thenReturn(new Player()); // Assume user exists
      doNothing().when(userService).updateUsernameBirthdate(userId, username, LocalDate.parse(birthdate));

      // Create a request body string with username and birthdate
      String requestBody = "{\"username\": \"" + username + "\", \"birthdate\": \"" + birthdate + "\"}";

      // Perform PUT request to /users/{id}
      mockMvc.perform(put("/users/{id}", username)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(requestBody))
              .andExpect(status().isNoContent());

      // Verify that the userService's updateUsernameBirthdate method is called with the correct arguments
      verify(userService).updateUsernameBirthdate(userId, username, LocalDate.parse(birthdate));
    }
*/
    /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   */
/*
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}

 */