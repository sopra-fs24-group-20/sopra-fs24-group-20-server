package ch.uzh.ifi.hase.soprafs24.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
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


    @ElementCollection
    @CollectionTable(name = "player_answers", joinColumns = @JoinColumn(name = "player_answer_id"))
    @MapKeyColumn(name = "username")
    private Map<String, List<PlayerCategoryResponse>> roundAnswers = new HashMap<>();
    // Constructors, getters, and setters

    @Embeddable
    public static class PlayerCategoryResponse {
        private String category;
        private CategoryAnswer categoryAnswer;

        // Constructors, getters, and setters
        public PlayerCategoryResponse() {}

        public PlayerCategoryResponse(String category, CategoryAnswer categoryAnswer) {
            this.category = category;
            this.categoryAnswer = categoryAnswer;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public CategoryAnswer getCategoryAnswer() {
            return categoryAnswer;
        }

        public void setCategoryAnswer(CategoryAnswer categoryAnswer) {
            this.categoryAnswer = categoryAnswer;
        }
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


    public Map<String, List<PlayerCategoryResponse>> getRoundAnswers() {
        return roundAnswers;
    }

    public void setRoundAnswers(Map<String, List<PlayerCategoryResponse>> roundAnswers) {
        this.roundAnswers = roundAnswers;
    }

}
