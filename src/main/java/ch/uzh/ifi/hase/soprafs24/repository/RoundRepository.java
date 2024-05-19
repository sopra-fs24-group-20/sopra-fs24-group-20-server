package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
@Repository("roundRepository")
public interface RoundRepository extends JpaRepository<Round, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Round r WHERE r.id = :id")
    Round findAndLockById(@Param("id") Long id);

    Optional<Round> findTopByOrderByIdDesc();
    List<Round> findByGameId(Long gameId);
    Optional<Round> findTopByGameIdOrderByIdDesc(Long gameId);
}

