package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
