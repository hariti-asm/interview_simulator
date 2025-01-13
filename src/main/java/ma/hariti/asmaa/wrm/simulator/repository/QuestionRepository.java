package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySessionId(Long sessionId);
    List<Question> findBySessionIdOrderByIdAsc(Long sessionId);
}

