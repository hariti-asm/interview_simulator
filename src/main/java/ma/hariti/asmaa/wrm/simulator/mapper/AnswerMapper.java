package ma.hariti.asmaa.wrm.simulator.mapper;

import ma.hariti.asmaa.wrm.simulator.dto.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(target = "questionId", source = "question.id")
    AnswerDTO toDTO(Answer answer);

    @Mapping(target = "question", ignore = true)
    Answer toEntity(AnswerDTO answerDTO);
}
