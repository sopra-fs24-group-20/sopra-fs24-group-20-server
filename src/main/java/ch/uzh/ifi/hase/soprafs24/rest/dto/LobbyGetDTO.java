package ch.uzh.ifi.hase.soprafs24.rest.dto;
import java.util.List;


public class LobbyGetDTO {
    private String lobbyName;
    private int roundDuration;
    private List<String> categories;
    private int rounds;
    private String gameMode;
    private boolean autoCorrectMode;

    // Getter-Funktionen
    public String getLobbyName() {
        return lobbyName;
    }


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

    public boolean isAutoCorrectMode() {
        return autoCorrectMode;
    }
    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

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
}

