package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;

import java.util.List;

public interface AIInterviewService {
   InterviewSessionDTO startNewSession( Long userId,String position, String specialization, String experienceLevel);

    AnswerDTO processAnswer( Long userId, Long sessionId, Long questionId, String answer);

    QuestionDTO generateNextQuestion( Long userId,Long sessionId);
    void deleteInterview(Long userId, Long sessionId);

    List<QuestionDTO> getQuestionsBySessionId(Long userId, Long sessionId);

    InterviewSessionDTO getInterviewById(Long userId, Long sessionId);
}
