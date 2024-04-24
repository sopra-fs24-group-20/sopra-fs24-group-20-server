package ch.uzh.ifi.hase.soprafs24.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Struct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Entity
@Table(name = "ROUND")
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private char assignedLetter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @JsonIgnore
    private Game game;

    @Type(type = "text")
    @Column(columnDefinition = "text")
    private String playerAnswers;

    @Column(columnDefinition = "text")
    private String roundPoints;  // This will store the JSON string

    // Constructors, getters, and setters

    public String getRoundPoints() {
        return roundPoints;
    }

    public void setRoundPoints(String pointsJson) {
        this.roundPoints = pointsJson;
    }
    public String getPlayerAnswers() {
        return playerAnswers;
    }

    public void setPlayerAnswers(String playerAnswers) {
        this.playerAnswers = playerAnswers;
    }
    public Round() {
        this.assignedLetter = generateRandomLetter();
    }

    private char generateRandomLetter() {
        return (char) ('A' + new Random().nextInt(26));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public char getAssignedLetter() {
        return assignedLetter;
    }

    public void setAssignedLetter(char assignedLetter) {
        this.assignedLetter = assignedLetter;
    }


    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
    //public Lobby getLobby(){return getGame().getLobby();}
}
