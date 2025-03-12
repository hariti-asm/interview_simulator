package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.response.PerformanceData;

import java.util.List;
import java.util.Map;

public interface AIInterviewService {
   InterviewSessionDTO startNewSession( Long userId,String position, String specialization, String experienceLevel);

    AnswerDTO processAnswer( Long userId, Long sessionId, Long questionId, String answer);

    QuestionDTO generateNextQuestion( Long userId,Long sessionId);
    void deleteInterview(Long userId, Long sessionId);

    List<QuestionDTO> getQuestionsBySessionId(Long userId, Long sessionId);

    InterviewSessionDTO getInterviewById(Long userId, Long sessionId);
 List<PerformanceData> getPerformanceBySkill(Long userId);
 Map<String, Object>getOverallPerformance(Long userId);
 Map<String, Object> getOverallPerformanceData(Long userId);



}
