package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.User;

import java.util.List;
import java.util.UUID;

public interface InterviewSessionService {
    InterviewSession startNewSession(Long userId, String position);
    InterviewSession getSession(Long sessionId);
    InterviewSession endSession(Long sessionId);
    List<InterviewSession> getUserSessions(Long userId);
    void updateSessionScore(Long sessionId, Float finalScore);
    void addStrongPoint(Long sessionId, String point);
    void addWeakPoint(Long sessionId, String point);
}