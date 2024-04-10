package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ROUND")
public class Round {

    @Column(nullable = false)
    private char assignedLetter;

    public void setAssignedLetter(char assignedLetter) {
        this.assignedLetter = assignedLetter;
    }

    public Round(char assignedLetter) {
        this.assignedLetter = assignedLetter;
    }
}
