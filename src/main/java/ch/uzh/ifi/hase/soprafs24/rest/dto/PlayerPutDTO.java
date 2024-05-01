package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class PlayerPutDTO {

    private List<StatisticDTO> stats;
    private Boolean ready;


    public boolean getReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }


    public List<StatisticDTO> getStats() {
        return stats;
    }

    public void setStats(List<StatisticDTO> stats) {
        this.stats = stats;
    }

}
