package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ma.hariti.asmaa.wrm.simulator.dto.request.SkillDTO;
import ma.hariti.asmaa.wrm.simulator.entity.Skill;
import ma.hariti.asmaa.wrm.simulator.mapper.SkillMapper;
import ma.hariti.asmaa.wrm.simulator.repository.SkillRepository;
import ma.hariti.asmaa.wrm.simulator.service.SkillService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillServiceDefault implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public SkillDTO createSkill(SkillDTO skillDTO) {
        skillDTO.setId(null);

        Skill skill = SkillMapper.INSTANCE.toEntity(skillDTO);
        skill = skillRepository.save(skill);
        return skillMapper.toDto(skill);
    }

    @Override
    public SkillDTO getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill not found with id: " + id));
        return skillMapper.toDto(skill);
    }

    @Override
    public List<SkillDTO> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SkillDTO updateSkill(Long id, SkillDTO skillDTO) {
        Skill existingSkill = skillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Skill not found with id: " + id));

        existingSkill.setName(skillDTO.getName());
        existingSkill.setDescription(skillDTO.getDescription());

        Skill updatedSkill = skillRepository.save(existingSkill);
        return skillMapper.toDto(updatedSkill);
    }

    @Override
    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new EntityNotFoundException("Skill not found with id: " + id);
        }
        skillRepository.deleteById(id);
    }
}
