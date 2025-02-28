package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByUserId(Long userId);
    List<InterviewSession> findByUserIdOrderByStartTimeDesc(Long userId);
    Optional<InterviewSession> findByIdAndUserId(Long id, Long userId);

}