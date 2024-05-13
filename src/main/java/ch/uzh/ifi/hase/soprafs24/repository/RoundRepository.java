package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("roundRepository")
public interface RoundRepository extends JpaRepository<Round, Long> {
    Optional<Round> findTopByOrderByIdDesc();
    List<Round> findByGameId(Long gameId);
    Optional<Round> findTopByGameIdOrderByIdDesc(Long gameId);
}

