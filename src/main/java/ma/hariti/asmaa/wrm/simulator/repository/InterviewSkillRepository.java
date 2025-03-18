package ma.hariti.asmaa.wrm.simulator.repository;

import ma.hariti.asmaa.wrm.simulator.entity.InterviewSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface InterviewSkillRepository extends JpaRepository<InterviewSkill, Long> {
    List<InterviewSkill> findByInterviewIdIn(Set<Long> interviewIds);
    boolean existsByInterviewIdAndSkillId(Long interviewId, Long skillId);
    List<InterviewSkill> findByInterviewId(Long interviewId);
}

