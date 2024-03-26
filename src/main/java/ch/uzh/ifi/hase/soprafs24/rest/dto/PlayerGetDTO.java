package ch.uzh.ifi.hase.soprafs24.rest.dto;


import ch.uzh.ifi.hase.soprafs24.entity.Statistic;

import java.time.LocalDate;
import java.util.List;

public class PlayerGetDTO {

  private boolean ready;
  private String password;
  private String username;
    private List<StatisticDTO> stats;

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

  public List<StatisticDTO> getStats() {
    return stats;
  }

  public void setStats(List<StatisticDTO> stats) {
    this.stats = stats;
  }


}
