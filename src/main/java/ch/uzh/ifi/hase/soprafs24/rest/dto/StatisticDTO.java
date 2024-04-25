package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class StatisticDTO {

    private List<String> answer;
    private List<Integer> points;
    private List<Integer> veto;
    private List<Integer> bonus;

    // Getters and setters

    public List<String> getAnswer() {
        return answer;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    public List<Integer> getVeto() {
        return veto;
    }

    public void setVeto(List<Integer> veto) {
        this.veto = veto;
    }

    public List<Integer> getBonus() {
        return bonus;
    }

    public void setBonus(List<Integer> bonus) {
        this.bonus = bonus;
    }
}
