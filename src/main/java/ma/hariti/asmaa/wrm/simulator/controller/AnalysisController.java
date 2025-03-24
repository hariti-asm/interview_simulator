package ma.hariti.asmaa.wrm.simulator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.service.AIAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AIAnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, List<String>>> analyzeAnswer(
            @RequestParam String questionContent,
            @RequestParam String userAnswer,
            @RequestParam String expectedAnswer) {

        Map<String, List<String>> analysis = analysisService.analyzeAnswer(
                questionContent, userAnswer, expectedAnswer);

        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/sessions/{sessionId}/answers/{answerId}/update-points")
    public ResponseEntity<Map<String, Object>> updateSessionPoints(
            @PathVariable Long sessionId,
            @PathVariable Long answerId) {

        boolean success = analysisService.updateSessionPoints(sessionId, answerId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ?
                "Session points updated successfully" :
                "Failed to update session points");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/strong-points")
    public ResponseEntity<Map<String, Object>> addStrongPoint(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> request) {

        String point = request.get("point");
        if (point == null || point.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Point cannot be empty"
            ));
        }

        analysisService.addStrongPoint(sessionId, point);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Strong point added successfully"
        ));
    }

    @PostMapping("/sessions/{sessionId}/weak-points")
    public ResponseEntity<Map<String, Object>> addWeakPoint(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> request) {

        String point = request.get("point");
        if (point == null || point.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Point cannot be empty"
            ));
        }

        analysisService.addWeakPoint(sessionId, point);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Weak point added successfully"
        ));
    }
}

