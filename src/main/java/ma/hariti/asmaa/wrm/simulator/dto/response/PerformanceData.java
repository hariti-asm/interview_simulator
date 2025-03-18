package ma.hariti.asmaa.wrm.simulator.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.entity.embeddable.SkillScore;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceData {
    private String skill;
    private String skillName;
    private double score;
    private int questionCount;
    private List<SkillScore> scores = new ArrayList<>();
}