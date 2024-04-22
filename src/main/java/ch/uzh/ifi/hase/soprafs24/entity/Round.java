package ch.uzh.ifi.hase.soprafs24.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashMap;
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

    @ElementCollection
    @CollectionTable(name = "player_answers", joinColumns = @JoinColumn(name = "round_id"))
    @MapKeyColumn(name = "player_id")
    @Column(name = "answer_info") // Rename to a more descriptive name
    private Map<String, Answer> roundAnswers = new HashMap<>();

    // Constructors, getters, and setters


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

    public Map<String, Answer> getRoundAnswers() {
        return roundAnswers;
    }

    public void setRoundAnswers(Map<String, Answer> roundAnswers) {
        this.roundAnswers = roundAnswers;
    }
}
