package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface AnswerRepository  extends JpaRepository<Answer, Long> {
    Optional<Answer> findByQuestionId(Long questionId);

}
