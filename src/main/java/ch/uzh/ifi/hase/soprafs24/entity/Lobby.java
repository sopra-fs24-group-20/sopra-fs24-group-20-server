package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LOBBY")
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lobbyName;

    @Column
    private String lobbyPassword; // For simplicity, stored in plaintext. Consider encryption for real applications.

    @Column(nullable = true)
    private int roundDuration;

    @Column(nullable = true)
    private int rounds;

    @Column(nullable = true)
    private int gameMode;

    @Column(nullable = true)
    private Boolean autoCorrectMode;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> categories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Character> excludedChars = new ArrayList<>();

    //@Enumerated(EnumType.STRING)
    //@Column(nullable = false)
    //private GameStatus gameStatus;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id", referencedColumnName = "id")
    private Game game;

    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Player> players = new ArrayList<>();

    // Other settings and fields as needed

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public Boolean getautoCorrectMode() {
        return autoCorrectMode;
    }

    public void setautoCorrectMode(Boolean autoCorrectMode) {
        this.autoCorrectMode = autoCorrectMode;
    }

    public String getLobbyPassword() {
        return lobbyPassword;
    }

    public void setLobbyPassword(String lobbyPassword) {
        this.lobbyPassword = lobbyPassword;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    public int getgameMode() {
        return gameMode;
    }

    public void setgameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public int getrounds() {
        return rounds;
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


    // Add methods to manage players in the lobby if needed
    // ...

}
