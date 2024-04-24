package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.Embeddable;

@Embeddable
public class CategoryAnswer {
    private String answer;
    private Integer points;

    // No-argument constructor required for Hibernate
    public CategoryAnswer() {
    }

    // Constructor for easy instantiation
    public CategoryAnswer(String answer, Integer points) {
        this.answer = answer;
        this.points = points;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
