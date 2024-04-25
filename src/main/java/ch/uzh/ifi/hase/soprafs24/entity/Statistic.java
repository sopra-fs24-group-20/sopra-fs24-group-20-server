package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "STATISTIC")
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary key
}