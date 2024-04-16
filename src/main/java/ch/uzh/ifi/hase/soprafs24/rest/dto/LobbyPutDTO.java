package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.LobbyStatus;
import javax.persistence.Lob;
import java.util.List;

public class LobbyPutDTO {
    private Integer roundDuration; // Use Integer instead of int
    private List<String> categories;
    private Integer rounds; // Use Integer instead of int
    private List<Character> excludedChars;
    private String gameMode;
    private Boolean autoCorrectMode; // Use Boolean instead of boolean
    private LobbyStatus lobbyStatus;

    // Getters and setters
    public Integer getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(Integer roundDuration) {
        this.roundDuration = roundDuration;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Integer getRounds() {
        return rounds;
    }

    public void setRounds(Integer rounds) {
        this.rounds = rounds;
    }

    public List<Character> getExcludedChars() {
        return excludedChars;
    }

    public void setExcludedChars(List<Character> excludedChars) {
        this.excludedChars = excludedChars;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public Boolean getAutoCorrectMode() {
        return autoCorrectMode;
    }

    public void setAutoCorrectMode(Boolean autoCorrectMode) {
        this.autoCorrectMode = autoCorrectMode;
    }

    public LobbyStatus getLobbyStatus() {
        return lobbyStatus;
    }

    public void setLobbyStatus(LobbyStatus lobbyStatus) {
        this.lobbyStatus = lobbyStatus;
    }
}
