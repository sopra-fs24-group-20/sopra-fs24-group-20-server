package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }


    @GetMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUserById(@PathVariable Long id) {
        // fetch user by id
        User user = userService.getUserById(id);

        // convert user to the API representation
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO login(@RequestBody UserPostDTO userPostDTO) {
        // Retrieve user from database based on the username
        User user = userService.authenticate(userPostDTO.getUsername(), userPostDTO.getPassword());
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

  @PutMapping("/logout/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
    public void logout(@PathVariable Long id){
      userService.updateUserStatus(id, UserStatus.OFFLINE);

  }


    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setUsernameBirthdate(@PathVariable Long id, @RequestBody Map<String, Object> requestBody) {
        String username = (String) requestBody.get("username");
        String birthdateString = (String) requestBody.get("birthdate");

        LocalDate birthdate = null;
        if (birthdateString != null && !birthdateString.isEmpty()) {
            birthdate = LocalDate.parse(birthdateString);
        }

        userService.updateUsernameBirthdate(id, username, birthdate);
    }


  @PutMapping("/users/{id}/birthdate")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
    public User setBirthdate(@PathVariable Long id, @RequestBody LocalDate birthdate){
      User user = userService.updateBirthdate(id, birthdate);
      return user;
  }

    @PutMapping("/users/{id}/username")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public User setUsername(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        User user = userService.updateUsername(id, username);
        return user;
    }




}
