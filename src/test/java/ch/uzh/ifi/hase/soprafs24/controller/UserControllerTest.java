package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
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

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    // user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        // .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

    @Test
    public void createUser_invalidInput_userNotCreated() throws Exception{
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setPassword("Test User");
        userPostDTO.setUsername("testUsername");

        given(userService.createUser(any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                // We expect the request to fail with a 409 Conflict status
                .andExpect(status().isConflict());
    }

    @Test
    public void givenUser_whenGetUser_thenReturnJsonArray() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);
        user.setBirthdate(LocalDate.of(2001,3,24));
        user.setCreationDate(LocalDate.of(2024,2,28));

        // this mocks the UserService -> we define above what the userService should
        // return when getUsers() is called
        given(userService.getUserById(user.getId())).willReturn(user);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
                .andExpect(jsonPath("$.birthdate", is(user.getBirthdate().toString())))
                .andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())));
    }


    @Test
    public void notGivenUser_whenGetUser_error() throws Exception {
        // this mocks the UserService -> we define above what the userService should
        // return when getUsers() is called
        given(userService.getUserById(2L)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{userId}", 2L)
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }




    @Test
    public void setUsernameBirthdate_UserUpdated_Returns204() throws Exception {
        Long userId = 1L;
        String username = "testUser";
        String birthdate = "1990-01-01";

        // Mock userService behavior
        when(userService.getUserById(userId)).thenReturn(new User()); // Assume user exists
        doNothing().when(userService).updateUsernameBirthdate(userId, username, LocalDate.parse(birthdate));

        // Create a request body string with username and birthdate
        String requestBody = "{\"username\": \"" + username + "\", \"birthdate\": \"" + birthdate + "\"}";

        // Perform PUT request to /users/{id}
        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        // Verify that the userService's updateUsernameBirthdate method is called with the correct arguments
        verify(userService).updateUsernameBirthdate(userId, username, LocalDate.parse(birthdate));
    }

    @Test
    public void setUsernameBirthdate_UserNotFound_Returns404() throws Exception {
        Long userId = 1L;
        String username = "testUser";
        String birthdate = "1990-01-01";

        // Mock userService behavior to return null when findUserById is called with userId
        when(userService.getUserById(userId)).thenReturn(null);

        // Create a request body string with username and birthdate
        String requestBody = "{\"username\": \"" + username + "\", \"birthdate\": \"" + birthdate + "\"}";

        // Perform PUT request to /users/{id} with the mocked userId
        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }








    /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   *
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}