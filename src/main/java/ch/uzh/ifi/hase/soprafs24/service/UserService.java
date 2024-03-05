package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);
    newUser.setCreationDate(LocalDate.now());

    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }



  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

      if (userByUsername != null) {
          throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
      }
  }
    public User checkforUser(String username) {
      return userRepository.findByUsername(username);
    }

    public User authenticate(String username, String password){
        List<User> allUsers = getUsers();
        for (User user : allUsers){
            if (Objects.equals(username, user.getUsername()) && Objects.equals(password, user.getPassword())){
                user.setStatus(UserStatus.ONLINE);
                return user;
            }

        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not registered user");
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "user with "+id+" was not found"));
    }


    public User updateUserStatus(Long id, UserStatus status) {
        // Find user by username
        User user = getUserById(id);
        if (user == null) {
            // Handle case where user is not found
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        // Update user's status
        user.setStatus(status);

        // Save the updated user
        return user;
    }


    public void updateUsernameBirthdate (Long id, String username, LocalDate birthdate){
      User user = getUserById(id);
      if (user == null){
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id);
      }
      User otherUserwithUsername = checkforUser(username);
      if (otherUserwithUsername != null && !otherUserwithUsername.getId().equals(id)){
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already in use");
      }
      if (birthdate != null){
          user.setBirthdate(birthdate);
      }
      user.setUsername(username);


    }

    public User updateUsername(Long id, String username){
        User user = getUserById(id);
        if (user == null) {
            // Handle case where user is not found
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        if (checkforUser(username) != null){
            throw new IllegalArgumentException("Username already in use");
        }

        // Update user's status
        user.setUsername(username);

        // Save the updated user
        return user;

    }

    public User updateBirthdate(Long id, LocalDate birthdate){
      User user = getUserById(id);
        if (user == null) {
            // Handle case where user is not found
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        // Update user's status
        user.setBirthdate(birthdate);

        // Save the updated user
        return user;

    }
}
