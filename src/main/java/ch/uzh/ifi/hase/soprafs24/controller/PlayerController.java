package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.PlayerService;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Player Controller
 * This class is responsible for handling all REST request that are related to
 * the Player.
 * The controller will receive the request and delegate the execution to the
 * PlayerService and finally return the result.
 */
@RestController
public class PlayerController {
    private static final Random random = new Random();

    private final PlayerService PlayerService;
    @Autowired
    private PlayerRepository playerRepository;



    @Autowired
    public PlayerController(PlayerService PlayerService, PlayerRepository playerRepository) {
        this.PlayerService = PlayerService;
        this.playerRepository = playerRepository;
    }

    @GetMapping("/players")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PlayerGetDTO> getAllPlayers() {
        // Fetch all Players in the internal representation
        List<Player> players = PlayerService.getPlayers();
        List<PlayerGetDTO> playerGetDTOs = new ArrayList<>();

        // Convert each Player to the API representation
        for (Player player : players) {
            playerGetDTOs.add(DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player));
        }
        return playerGetDTOs;
    }

    @PutMapping("/players/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public ResponseEntity<Void> updatePlayer(@PathVariable String username, @RequestBody PlayerPutDTO playerPutDTO) {
        // Convert DTO to entity
        Player updatedPlayerInfo = DTOMapper.INSTANCE.convertPlayerPutDTOtoEntity(playerPutDTO);

        // Call service to update player
        Player updatedPlayer = PlayerService.updatePlayer(username, updatedPlayerInfo);

        playerRepository.save(updatedPlayer);

        // Convert updated entity back to DTO
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/players")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PlayerGetDTO createPlayer(@RequestBody PlayerPostDTO playerPostDTO) {
        String username = playerPostDTO.getUsername();
        String password = playerPostDTO.getPassword();
        // Check if a non-system-generated username starts with "Guest:"
        if (username != null && username.trim().startsWith("Guest:")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usernames starting with 'Guest:' are reserved and cannot be manually set.");
        }

        // Generate a guest username if no username is provided
        if (username == null || username.trim().isEmpty()) {
            username = generateUniqueGuestUsername();
            password = username;
        }

        Player playerInput = DTOMapper.INSTANCE.convertPlayerPostDTOtoEntity(playerPostDTO);
        playerInput.setUsername(username);
        playerInput.setPassword(password);
        Player createdPlayer = PlayerService.createPlayer(playerInput.getUsername(), playerInput.getPassword());
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(createdPlayer);
    }
    private String generateUniqueGuestUsername() {
        String username;
        do {
            username = generateGuestUsername();
        } while (playerRepository.existsByUsername(username)); // Properly use the repository method
        return username;
    }
    public static String generateGuestUsername() {
        // List of things nouns
        List<String> nounList = Arrays.asList(
                "Stone", "Flame", "Wave", "Star", "Leaf", "Rain",
                "Snow", "Wind", "Echo", "Tree", "Moon", "Rock",
                "Dust", "Breeze", "Sky", "River", "Cloud"
        );

        // List of adjectives
        List<String> adjectiveList = Arrays.asList(
                "Cute", "Quirky", "Dramatic", "Chubby", "Fluffy", "Silly", "Dizzy",
                "Big", "Small", "Smart", "Prickly", "Funky", "Sassy", "Happy", "Sad",
                "Kind", "Criminal"
        );

        // Combine a random adjective and noun
        String descriptor = adjectiveList.get(random.nextInt(adjectiveList.size())) +
                " " +
                nounList.get(random.nextInt(nounList.size()));

        return "Guest: " + descriptor;
    }


    @GetMapping("/players/{Username}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO getPlayerByUsername(@PathVariable String Username) {
        // fetch Player by Username
        Player Player = PlayerService.getPlayerByUsername(Username);
        if (Player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found with Username: " + Username);
        }
        // convert Player to the API representation
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(Player);
    }


    @PostMapping("/players/login")
    @ResponseBody
    public ResponseEntity<PlayerGetDTO> login(@RequestBody PlayerPostDTO loginDTO) {
        Player player = PlayerService.LogInPlayer(loginDTO.getUsername(), loginDTO.getPassword());
        PlayerGetDTO playerGetDTO = DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player);
        return ResponseEntity.ok(playerGetDTO);
    }
    @PostMapping("/players/logout")
    @ResponseBody
    public ResponseEntity<PlayerGetDTO> logout(@RequestBody PlayerPostDTO loginDTO) {
        Player player = PlayerService.LogOutPlayer(loginDTO.getUsername());
        PlayerGetDTO playerGetDTO = DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player);
        return ResponseEntity.ok(playerGetDTO);
    }

}
