package ma.hariti.asmaa.wrm.simulator.service;

import java.util.List;
import java.util.Map;


public interface AIAnalysisService {
    Map<String, List<String>> analyzeAnswer(String questionContent, String userAnswer, String expectedAnswer);
    boolean updateSessionPoints(Long sessionId, Long answerId);
    void addStrongPoint(Long sessionId, String point);
    void addWeakPoint(Long sessionId, String point);
}

