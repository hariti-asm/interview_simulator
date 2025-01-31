package ma.hariti.asmaa.wrm.simulator.mapper;


import ma.hariti.asmaa.wrm.simulator.dto.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface QuestionMapper {
    @Mapping(source = "session.id", target = "sessionId")
    QuestionDTO toDTO(Question question);
    Question toEntity(QuestionDTO dto);

    List<QuestionDTO> toDTOList(List<Question> questions);
}