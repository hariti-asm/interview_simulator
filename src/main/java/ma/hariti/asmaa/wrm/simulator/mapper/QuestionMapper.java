package ma.hariti.asmaa.wrm.simulator.mapper;


import ma.hariti.asmaa.wrm.simulator.dto.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    @Mapping(target = "sessionId", source = "session.id")
    QuestionDTO toDTO(Question question);

    @Mapping(target = "session", ignore = true)
    @Mapping(target = "answer", ignore = true)
    Question toEntity(QuestionDTO questionDTO);
}