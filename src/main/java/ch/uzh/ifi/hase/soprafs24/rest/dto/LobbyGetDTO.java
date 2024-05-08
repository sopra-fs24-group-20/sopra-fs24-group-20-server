package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Player;

import java.util.List;


public class LobbyGetDTO {
    private String lobbyName;
    private Player lobbyOwner;
    private Integer roundDuration;
    private List<String> categories;
    private Integer rounds;
    private String gameMode;
    private Boolean autoCorrectMode;
    private LobbyStatus lobbyStatus;
    private List<Character> excludedChars;

    // Getter-Funktionen
    public String getLobbyName() {
        return lobbyName;
    }

    public Player getLobbyOwner(){return lobbyOwner;}

    public int getRoundDuration() {
        return roundDuration;
    }

    public List<String> getCategories() {
        return categories;
    }

    public int getRounds() {
        return rounds;
    }

    public String getGameMode() {
        return gameMode;
    }

    public LobbyStatus getLobbyStatus() {
        return lobbyStatus;
    }
    public List<Character> getExcludedChars() {
        return excludedChars;
    }

    public boolean isAutoCorrectMode() {
        return autoCorrectMode;
    }
    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public void setLobbyOwner(Player lobbyOwner){ this.lobbyOwner = lobbyOwner;}

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public void setAutoCorrectMode(boolean autoCorrectMode) {
        this.autoCorrectMode = autoCorrectMode;
    }
    public void setLobbyStatus(LobbyStatus lobbyStatus) {
        this.lobbyStatus = lobbyStatus;
    }
    public void setExcludedChars(List<Character> excludedChars) {
        this.excludedChars = excludedChars;
    }
}

