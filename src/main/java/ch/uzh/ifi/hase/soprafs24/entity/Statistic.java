package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "STATISTIC")
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary key

    @ElementCollection
    private List<String> Answer;

    @ElementCollection
    private List<Integer> Points;

    @ElementCollection
    private List<Integer> Veto;

    @ElementCollection
    private List<Integer> Bonus;

    // Getters and setters for Answer
    public List<String> getAnswer() {
        return Answer;
    }

    public void setAnswer(List<String> answer) {
        Answer = answer;
    }

    // Getters and setters for Points
    public List<Integer> getPoints() {
        return Points;
    }

    public void setPoints(List<Integer> points) {
        Points = points;
    }

    // Getters and setters for Veto
    public List<Integer> getVeto() {
        return Veto;
    }

    public void setVeto(List<Integer> veto) {
        Veto = veto;
    }

    // Getters and setters for Bonus
    public List<Integer> getBonus() {
        return Bonus;
    }

    public void setBonus(List<Integer> bonus) {
        Bonus = bonus;
    }

    // Constructors, hashCode, equals, toString, etc. can be added as needed
}