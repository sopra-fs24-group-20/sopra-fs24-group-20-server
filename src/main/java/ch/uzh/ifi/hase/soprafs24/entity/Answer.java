package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.Embeddable;
import java.util.HashMap;
import java.util.Map;

@Embeddable
public class Answer {
    private Map<String, CategoryAnswer> roundAnswers = new HashMap<>();

    public Map<String, CategoryAnswer> getPlayerAnswers() {
        return roundAnswers;
    }

    public void setPlayerAnswers(Map<String, CategoryAnswer> playerAnswers) {
        this.roundAnswers = playerAnswers;
    }

    public void setScoreForPlayerAnswer(String playerId, String category, int newPoints) {
        CategoryAnswer categoryAnswer = roundAnswers.computeIfAbsent(category, k -> new CategoryAnswer());
        categoryAnswer.setPoints(newPoints);
    }
}
