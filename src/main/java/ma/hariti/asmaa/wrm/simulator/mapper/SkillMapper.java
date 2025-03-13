package ma.hariti.asmaa.wrm.simulator.mapper;

import ma.hariti.asmaa.wrm.simulator.dto.request.SkillDTO;
import ma.hariti.asmaa.wrm.simulator.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillMapper INSTANCE = Mappers.getMapper(SkillMapper.class);

    SkillDTO toDto(Skill skill);

    Skill toEntity(SkillDTO skillDTO);

    List<SkillDTO> toDtoList(List<Skill> skills);

    List<Skill> toEntityList(List<SkillDTO> skillDTOs);
}

