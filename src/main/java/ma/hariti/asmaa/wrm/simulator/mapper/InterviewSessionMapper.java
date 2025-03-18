package ma.hariti.asmaa.wrm.simulator.mapper;

import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSkill;
import ma.hariti.asmaa.wrm.simulator.entity.Skill;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class})
public interface InterviewSessionMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "position", source = "position")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    @Mapping(target = "finalScore", source = "finalScore")
    @Mapping(target = "questions", source = "questions")
    @Mapping(target = "strongPoints", source = "strongPoints")
    @Mapping(target = "weakPoints", source = "weakPoints")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "skills", ignore = true)
    InterviewSessionDTO toDTO(InterviewSession session);

    @AfterMapping
    default void mapSkills(InterviewSession session, @MappingTarget InterviewSessionDTO dto) {
        if (session.getInterviewSkills() != null) {
            List<String> skillNames = session.getInterviewSkills().stream()
                    .map(InterviewSkill::getSkill)
                    .map(Skill::getName)
                    .collect(Collectors.toList());
            dto.setSkills(skillNames);
        }
    }

    @Mapping(target = "interviewContext", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "experienceLevel", ignore = true)
    @Mapping(target = "interviewSkills", ignore = true)
    InterviewSession toEntity(InterviewSessionDTO dto);

    List<InterviewSessionDTO> toDTOList(List<InterviewSession> sessions);
}

