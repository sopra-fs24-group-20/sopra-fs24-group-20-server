package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class LobbyPutDTO {
    private int roundDuration;
    private List<String> categories;
    private int rounds;
    private List<Character> excludedChars;
    private String gameMode;
    private boolean autoCorrectMode;

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public void setExcludedChars(List<Character> excludedChars) {
        this.excludedChars = excludedChars;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public void setAutoCorrectMode(boolean autoCorrectMode) {
        this.autoCorrectMode = autoCorrectMode;
    }
    public Integer getRoundDuration() {
        return roundDuration;
    }

    public List<String> getCategories() {
        return categories;
    }

    public Integer getRounds() {
        return rounds;
    }

    public List<Character> getExcludedChars() {
        return excludedChars;
    }

    public String getGameMode() {
        return gameMode;
    }

    public Boolean getAutoCorrectMode() {
        return autoCorrectMode;
    }


}

