
package ma.hariti.asmaa.wrm.simulator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "interview_sessions")
public class InterviewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Position is required")
    @Size(min = 2, max = 100, message = "Position must be between 2 and 100 characters")
    private String position;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    @DecimalMax(value = "100.0", message = "Score cannot exceed 100")
    private Float finalScore;

    @Column(length = 10000)
    private String interviewContext;

    private String specialization;
    private String experienceLevel;

    @ElementCollection
    @CollectionTable(name = "strong_points",
            joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "point")
    private List<String> strongPoints = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "weak_points",
            joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "point")
    private List<String> weakPoints = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewSkill> interviewSkills = new ArrayList<>();
}