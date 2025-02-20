package ma.hariti.asmaa.wrm.simulator.mapper;

import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.RegisterUserRequest;
import ma.hariti.asmaa.wrm.simulator.entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "score", target = "score")
    @Mapping(source = "improvementSuggestions", target = "improvementSuggestions")
    @Mapping(source = "id", target = "id")
    AnswerDTO toDTO(Answer answer);

    @Mapping(target = "content", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "improvementSuggestions", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questionId", ignore = true)
    @Mapping(source = "feedback", target = "feedback")
    @Mapping(source = "followUpQuestion", target = "followUpQuestion")
     AnswerDTO toDTO(String feedback, String followUpQuestion);

    @Mapping(target = "question.id", source = "questionId")
    Answer toEntity(AnswerDTO dto);
}