package ch.uzh.ifi.hase.soprafs24.rest.dto;
import java.util.List;


public class LobbyPostDTO {
    private String lobbyName;
    private String lobbyPassword;
    private String username;
    private int roundDuration;
    private List<String> categories;
    private int rounds;
    private List<Character> excludedChars;
    private String gameMode;
    private boolean autoCorrectMode;

    // Setter-Funktionen
    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyPassword(String lobbyPassword) {
        this.lobbyPassword = lobbyPassword;
    }

    public String getLobbyPassword() { return lobbyPassword; }

    public String getUsername() { return username; }


    // Weitere Setter-Funktionen für die anderen Attribute (für die Erstellung der Lobby)
}

