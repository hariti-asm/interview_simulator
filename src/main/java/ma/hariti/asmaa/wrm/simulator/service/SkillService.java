package ma.hariti.asmaa.wrm.simulator.service;


import ma.hariti.asmaa.wrm.simulator.dto.request.SkillDTO;

import java.util.List;

public interface SkillService {
    SkillDTO createSkill(SkillDTO skillDTO);
    SkillDTO getSkillById(Long id);
    List<SkillDTO> getAllSkills();
    SkillDTO updateSkill(Long id, SkillDTO skillDTO);
    void deleteSkill(Long id);
}
