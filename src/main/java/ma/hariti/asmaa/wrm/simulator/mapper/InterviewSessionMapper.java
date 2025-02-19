package ma.hariti.asmaa.wrm.simulator.mapper;

import ma.hariti.asmaa.wrm.simulator.dto.request.ForgotPasswordRequest;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
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
    ForgotPasswordRequest.InterviewSessionDTO toDTO(InterviewSession session);

    @Mapping(target = "interviewContext", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "experienceLevel", ignore = true)
    InterviewSession toEntity(ForgotPasswordRequest.InterviewSessionDTO dto);

    List<ForgotPasswordRequest.InterviewSessionDTO> toDTOList(List<InterviewSession> sessions);
}