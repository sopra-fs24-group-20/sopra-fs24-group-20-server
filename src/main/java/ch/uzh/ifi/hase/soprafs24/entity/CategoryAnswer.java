package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.Embeddable;

@Embeddable
public class CategoryAnswer {
    private String answer;
    private Integer points;

    // Constructors, getters, and setters...
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
