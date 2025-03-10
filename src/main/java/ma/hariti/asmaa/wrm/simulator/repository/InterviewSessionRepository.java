package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    Optional<InterviewSession> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT i.position, COUNT(i) FROM InterviewSession i GROUP BY i.position ORDER BY COUNT(i) ASC")
    List<Object[]> countInterviewsByPosition();

    List<InterviewSession> findByUserId(Long userId);
}