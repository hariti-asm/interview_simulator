package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);

    @Query("SELECT s FROM Skill s JOIN s.relevantPositions p WHERE p = :position")
    List<Skill> findByRelevantPositionsContaining(@Param("position") String position);

    // Add a method to find skills by category
    List<Skill> findByCategory(String category);

    // Add a method to find skills by partial position match
    @Query("SELECT s FROM Skill s JOIN s.relevantPositions p WHERE LOWER(p) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Skill> findByPositionKeyword(@Param("keyword") String keyword);
}

