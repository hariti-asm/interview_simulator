package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.dto.request.*;

public interface AIInterviewService {
   InterviewSessionDTO startNewSession( Long userId,String position, String specialization, String experienceLevel);

    AnswerDTO processAnswer( Long userId, Long sessionId, Long questionId, String answer);

    QuestionDTO generateNextQuestion( Long userId,Long sessionId);
}
