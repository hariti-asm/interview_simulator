package ma.hariti.asmaa.wrm.simulator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Skill name is required")
    @Size(min = 2, max = 100, message = "Skill name must be between 2 and 100 characters")
    @Column(unique = true)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Category is required")
    @Size(min = 2, max = 50, message = "Category must be between 2 and 50 characters")
    private String category;

    @Column(name = "skill_type")
    private String skillType;

    @ElementCollection
    @CollectionTable(
            name = "skill_proficiency_levels",
            joinColumns = @JoinColumn(name = "skill_id")
    )
    @Column(name = "level")
    private List<String> proficiencyLevels = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "skill_keywords",
            joinColumns = @JoinColumn(name = "skill_id")
    )
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewSkill> interviewSkills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "skill_position_relevance",
            joinColumns = @JoinColumn(name = "skill_id")
    )
    @Column(name = "position_name")
    private List<String> relevantPositions = new ArrayList<>();

    private Integer weight = 1;
    private Boolean isActive = true;
}