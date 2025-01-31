package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findByIdAndSessionId(Long questionId, Long sessionId);
    @Query("SELECT q FROM Question q WHERE q.id = :questionId AND q.session.id = :sessionId")
    Optional<Question> findByIdAndSessionIdExplicitly(
            @Param("questionId") Long questionId,
            @Param("sessionId") Long sessionId
    );
}

