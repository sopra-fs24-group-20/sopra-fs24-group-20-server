package ch.uzh.ifi.hase.soprafs24.entity;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

import ch.uzh.ifi.hase.soprafs24.entity.Statistic;
/**
 * Internal Player Representation
 * This class composes the internal representation of the player and defines how
 * the player is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 */
@Entity
@Table(name = "PLAYER")
public class Player implements Serializable {
    @Id
    @Column(nullable = false, unique = true)
    private String username;
    @ManyToOne
    @JoinColumn(name = "lobbyId") // This column in the player table will refer to the lobby's id.
    private long lobbyId;

    @Column(nullable = false)
    private boolean ready;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Statistic> stats;


    @Column(nullable = true)
    private String token;

    @Column(nullable = false)
    private String password;

    // Getters and Setters




    public boolean getReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public List<Statistic> getStats() {
        return stats;
    }

    public void setStats(List<Statistic> stats) {
        this.stats = stats;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    // In Player class
    public long getLobbyId() {
        return this.lobbyId;
    }
    public void setLobbyId(long lobbyId) {
        this.lobbyId = lobbyId;
    }

}
