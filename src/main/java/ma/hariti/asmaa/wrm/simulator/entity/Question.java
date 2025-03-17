package ma.hariti.asmaa.wrm.simulator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.entity.enums.DifficultyLevel;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question content is required")
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String expectedAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @NotNull(message = "Interview session is required")
    private InterviewSession session;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, optional = true)
    private Answer answer;

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    private Integer weight = 1;
}