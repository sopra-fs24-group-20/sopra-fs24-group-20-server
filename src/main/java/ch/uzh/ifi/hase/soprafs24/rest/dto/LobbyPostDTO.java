package ch.uzh.ifi.hase.soprafs24.rest.dto;
import java.util.List;


public class LobbyPostDTO {
    private String lobbyName;
    private String lobbyPassword;
    private long id;
    private String username;
    private String password;
    private int roundDuration;
    private List<String> categories;
    private int rounds;

    private List<Character> excludedChars;
    private String gameMode;
    private boolean autoCorrectMode;
    private String ownerUsername; // New field for owner's username

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
    public void setLobbyId(long lobbyId) { this.id = lobbyId; }
    public long getLobbyId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() { return password; }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }


    // Weitere Setter-Funktionen für die anderen Attribute (für die Erstellung der Lobby)
}

