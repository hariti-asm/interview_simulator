package ma.hariti.asmaa.wrm.simulator.entity.embeddable;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillScore {
    private Long skillId;
    private Float score;
}