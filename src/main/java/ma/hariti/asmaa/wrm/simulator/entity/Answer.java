package ma.hariti.asmaa.wrm.simulator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Answer content is required")
    @Column(columnDefinition = "TEXT")
    private String content;

    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    @DecimalMax(value = "100.0", message = "Score cannot exceed 100")
    private Float score;

    @ElementCollection
    @CollectionTable(name = "improvement_suggestions",
            joinColumns = @JoinColumn(name = "answer_id"))
    @Column(name = "suggestion")
    private List<String> improvementSuggestions = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "question_id", nullable = false)
    @NotNull(message = "Question is required")
    private Question question;
}