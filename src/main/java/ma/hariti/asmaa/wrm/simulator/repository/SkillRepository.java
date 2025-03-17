package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByRelevantPositionsContaining(String position);
}
