package ch.uzh.ifi.hase.soprafs24.entity;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
    @JoinColumn(name = "lobby_id")
    @JsonBackReference
    private Lobby lobby;

    @Column(nullable = false)
    private Boolean ready;

    @Column(nullable = false)
    private String password;

    // Stats fields
    @Column(nullable = false)
    private int totalPoints;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int roundsPlayed;

    @Column(nullable = false)
    private double averagePointsPerRound;

    @Column(nullable = false)
    private int victories;

    // Getters and Setters
    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    // In Player class
    public Lobby getLobby() {
        return this.lobby;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public int getTotalPoints() {
        return this.totalPoints;
    }

    public void setTotalPoints(int totalpoints) {
        this.totalPoints = totalpoints;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public void setRoundsPlayed(int rounds) {
        this.roundsPlayed = rounds;
    }

    public double getAveragePointsPerRound() {
        BigDecimal bd = new BigDecimal(averagePointsPerRound);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void setAveragePointsPerRound(double averagePointsPerRound) {
        this.averagePointsPerRound = averagePointsPerRound;
    }

    public int getVictories() {
        return victories;
    }

    public void setVictories(int victories) {
        this.victories = victories;
    }
}
