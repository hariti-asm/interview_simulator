package ma.hariti.asmaa.wrm.simulator.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "interview_skills")
public class InterviewSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "interview_id")
    private InterviewSession interview;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;

}