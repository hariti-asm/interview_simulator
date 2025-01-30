package ma.hariti.asmaa.wrm.simulator.mapper;

import ma.hariti.asmaa.wrm.simulator.dto.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class})
public interface InterviewSessionMapper {
    @Mapping(target = "questions", source = "questions")
    InterviewSessionDTO toDTO(InterviewSession session);

    @Mapping(target = "interviewContext", ignore = true)
    InterviewSession toEntity(InterviewSessionDTO dto);

    List<InterviewSessionDTO> toDTOList(List<InterviewSession> sessions);
}


