package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LOBBY")
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lobbyId;

    @Column(nullable = false)
    private String lobbyName;

    @Column
    private String lobbyPassword; // For simplicity, stored in plaintext. Consider encryption for real applications.

    @Column
    private LobbyStatus lobbyStatus;
    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Player lobbyOwner; // Attribute for owner ensuring a bidirectional one-to-one relationship

    @Column(nullable = true)
    private int roundDuration;

    @Column(nullable = true)
    private int rounds;

    @Column(nullable = true)
    private String gameMode;

    @Column(nullable = true)
    private Boolean autoCorrectMode;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> categories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Character> excludedChars = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id", referencedColumnName = "id")
    private Game game;

    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Player> players = new ArrayList<>();

    // Other settings and fields as needed

    // Getters and setters
    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyId) {
        this.lobbyId = lobbyId;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public Boolean getAutoCorrectMode() {
        return autoCorrectMode;
    }

    public void setAutoCorrectMode(Boolean autoCorrectMode) {
        this.autoCorrectMode = autoCorrectMode;
    }

    public String getLobbyPassword() {
        return lobbyPassword;
    }

    public void setLobbyPassword(String lobbyPassword) {
        this.lobbyPassword = lobbyPassword;
    }
    
    public Player getLobbyOwner() {
        return lobbyOwner;
    }
    public void setLobbyOwner(Player lobbyOwner) {
        this.lobbyOwner = lobbyOwner;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public LobbyStatus getLobbyStatus() {
        return lobbyStatus;
    }


    public void rounds(int rounds) {
        this.rounds = rounds;
    }


    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<Character> getExcludedChars() {
        return excludedChars;
    }

    public void setExcludedChars(List<Character> excludedChars) {
        this.excludedChars = excludedChars;
    }
    {/*
    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }*/}

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
    // In Lobby class
    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getRounds() {
        return this.rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }
    public void setLobbyStatus(LobbyStatus lobbyStatus) {
        this.lobbyStatus = lobbyStatus;
    }

}
