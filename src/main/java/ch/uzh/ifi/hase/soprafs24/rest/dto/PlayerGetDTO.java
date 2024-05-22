package ch.uzh.ifi.hase.soprafs24.rest.dto;


import javax.persistence.Column;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PlayerGetDTO {

  private Boolean ready;
  private String password;
  private String username;
  private int totalPoints;
  private int level;
  private int roundsPlayed;
  private double averagePointsPerRound;
  private int victories;

  public boolean getReady() {
    return ready;
  }

  public void setReady(boolean ready) {
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
    BigDecimal bd = BigDecimal.valueOf(averagePointsPerRound);
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
