package ma.hariti.asmaa.wrm.simulator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import ma.hariti.asmaa.wrm.simulator.dto.request.AnswerDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.QuestionDTO;
import ma.hariti.asmaa.wrm.simulator.entity.Answer;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.Question;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import ma.hariti.asmaa.wrm.simulator.mapper.AnswerMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.InterviewSessionMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.QuestionMapper;
import ma.hariti.asmaa.wrm.simulator.repository.AnswerRepository;
import ma.hariti.asmaa.wrm.simulator.repository.InterviewSessionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.QuestionRepository;
import ma.hariti.asmaa.wrm.simulator.repository.UserRepository;
import ma.hariti.asmaa.wrm.simulator.service.AnswerService;
import ma.hariti.asmaa.wrm.simulator.service.serviceDefault.AIInterviewServiceDefault;
import ma.hariti.asmaa.wrm.simulator.service.serviceDefault.AIServiceDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AIInterviewServiceDefaultTest {

    @Mock
    private AIServiceDefault aiService;

    @Mock
    private InterviewSessionRepository sessionRepository;

    @Mock
    private InterviewSessionMapper sessionMapper;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private AnswerMapper answerMapper;

    @Mock
    private AnswerService answerService;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private AIInterviewServiceDefault aiInterviewService;

    private User user;
    private InterviewSession session;
    private InterviewSessionDTO sessionDTO;
    private Question question;
    private QuestionDTO questionDTO;
    private Answer answer;
    private AnswerDTO answerDTO;
    private List<Question> questionList;
    private List<QuestionDTO> questionDTOList;

    @BeforeEach
    void setUp() {
        // Initialize test data
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john.doe@example.com");

        session = new InterviewSession();
        session.setId(1L);
        session.setUser(user);
        session.setPosition("Software Developer");
        session.setSpecialization("Java");
        session.setExperienceLevel("Mid-level");
        session.setStartTime(LocalDateTime.now());
        session.setInterviewContext("Java developer interview");

        sessionDTO = new InterviewSessionDTO();
        sessionDTO.setId(1L);
        sessionDTO.setUserId(1L);
        sessionDTO.setPosition("Software Developer");


        question = new Question();
        question.setId(1L);
        question.setContent("What is polymorphism in Java?");
        question.setExpectedAnswer("Polymorphism is the ability of an object to take on many forms.");
        question.setSession(session);

        questionDTO = new QuestionDTO();
        questionDTO.setId(1L);
        questionDTO.setContent("What is polymorphism in Java?");
        questionDTO.setExpectedAnswer("Polymorphism is the ability of an object to take on many forms.");
        questionDTO.setSessionId(1L);

        answer = new Answer();
        answer.setId(1L);
        answer.setContent("Polymorphism allows methods to do different things based on the object it is acting upon.");
        answer.setQuestion(question);
        answer.setScore(85.0f);
        answer.setImprovementSuggestions(Arrays.asList("Add example", "Mention types of polymorphism"));

        answerDTO = new AnswerDTO();
        answerDTO.setId(1L);
        answerDTO.setContent("Polymorphism allows methods to do different things based on the object it is acting upon.");
        answerDTO.setQuestionId(1L);
        answerDTO.setScore(85.0f);
        answerDTO.setImprovementSuggestions(Arrays.asList("Add example", "Mention types of polymorphism"));
        answerDTO.setFeedback("Good answer but could be more detailed.");
        answerDTO.setFollowUpQuestion("Can you explain the difference between runtime and compile-time polymorphism?");

        questionList = new ArrayList<>();
        questionList.add(question);

        questionDTOList = new ArrayList<>();
        questionDTOList.add(questionDTO);

        session.setQuestions(questionList);
    }

    @Nested
    @DisplayName("Start New Session Tests")
    class StartNewSessionTests {

        @Test
        @DisplayName("Should start new session successfully")
        void shouldStartNewSessionSuccessfully() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(aiService.generateInitialContext(anyString(), anyString())).thenReturn("Java developer interview context");
            when(sessionRepository.save(any(InterviewSession.class))).thenReturn(session);
            when(sessionMapper.toDTO(session)).thenReturn(sessionDTO);

            // Act
            InterviewSessionDTO result = aiInterviewService.startNewSession(1L, "Software Developer", "Java", "Mid-level");

            // Assert
            assertNotNull(result);
            assertEquals(sessionDTO.getId(), result.getId());
            assertEquals(sessionDTO.getPosition(), result.getPosition());

            // Verify interactions
            verify(userRepository).findById(1L);
            verify(aiService).generateInitialContext("Software Developer", "Mid-level");
            verify(sessionRepository).save(any(InterviewSession.class));
            verify(sessionMapper).toDTO(session);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () ->
                    aiInterviewService.startNewSession(999L, "Software Developer", "Java", "Mid-level"));

            // Verify interactions
            verify(userRepository).findById(999L);
            verify(sessionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should propagate exception when session save fails")
        void shouldPropagateExceptionWhenSessionSaveFails() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(aiService.generateInitialContext(anyString(), anyString())).thenReturn("Java developer interview context");
            when(sessionRepository.save(any(InterviewSession.class))).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () ->
                    aiInterviewService.startNewSession(1L, "Software Developer", "Java", "Mid-level"));

            // Verify interactions
            verify(userRepository).findById(1L);
            verify(aiService).generateInitialContext("Software Developer", "Mid-level");
            verify(sessionRepository).save(any(InterviewSession.class));
        }
    }

    @Nested
    @DisplayName("Process Answer Tests")
    class ProcessAnswerTests {

        @Test
        @DisplayName("Should process answer successfully with new answer")
        void shouldProcessAnswerSuccessfullyWithNewAnswer() {
            // Arrange
            String userAnswer = "Polymorphism allows methods to do different things based on the object it is acting upon.";
            question.setAnswer(null); // No existing answer

            when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
            when(aiService.generateQuestionFeedback(anyString(), anyString())).thenReturn("Good answer but could be more detailed.");
            when(aiService.generateFollowUpQuestion(anyString(), anyString())).thenReturn("Can you explain the difference between runtime and compile-time polymorphism?");
            when(answerService.calculateScore(anyString(), anyString())).thenReturn(85.0f);
            when(answerService.generateImprovementSuggestions(anyString(), anyString())).thenReturn(Arrays.asList("Add example", "Mention types of polymorphism"));
            when(answerRepository.save(any(Answer.class))).thenReturn(answer);
            when(questionRepository.save(any(Question.class))).thenReturn(question);
            when(answerMapper.toDTO(answer)).thenReturn(answerDTO);

            // Act
            AnswerDTO result = aiInterviewService.processAnswer(1L, 1L, 1L, userAnswer);

            // Assert
            assertNotNull(result);
            assertEquals(answerDTO.getId(), result.getId());
            assertEquals(answerDTO.getContent(), result.getContent());
            assertEquals(answerDTO.getFeedback(), result.getFeedback());
            assertEquals(answerDTO.getFollowUpQuestion(), result.getFollowUpQuestion());

            // Verify interactions
            verify(sessionRepository).findById(1L);
            verify(questionRepository).findById(1L);
            verify(aiService).generateQuestionFeedback(question.getContent(), userAnswer);
            verify(aiService).generateFollowUpQuestion(question.getContent(), userAnswer);
            verify(answerService).calculateScore(userAnswer, question.getExpectedAnswer());
            verify(answerService).generateImprovementSuggestions(userAnswer, question.getExpectedAnswer());
            verify(answerRepository).save(any(Answer.class));
            verify(questionRepository).save(question);
            verify(answerMapper).toDTO(answer);
        }

        @Test
        @DisplayName("Should process answer successfully with existing answer")
        void shouldProcessAnswerSuccessfullyWithExistingAnswer() {
            // Arrange
            String userAnswer = "Updated answer about polymorphism.";
            question.setAnswer(answer); // Existing answer

            when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
            when(aiService.generateQuestionFeedback(anyString(), anyString())).thenReturn("Better answer now.");
            when(aiService.generateFollowUpQuestion(anyString(), anyString())).thenReturn("Can you provide an example?");
            when(answerService.calculateScore(anyString(), anyString())).thenReturn(90.0f);
            when(answerService.generateImprovementSuggestions(anyString(), anyString())).thenReturn(Arrays.asList("Add more examples"));
            when(answerRepository.save(any(Answer.class))).thenReturn(answer);
            when(questionRepository.save(any(Question.class))).thenReturn(question);
            when(answerMapper.toDTO(answer)).thenReturn(answerDTO);

            // Act
            AnswerDTO result = aiInterviewService.processAnswer(1L, 1L, 1L, userAnswer);

            // Assert
            assertNotNull(result);

            // Verify interactions
            verify(sessionRepository).findById(1L);
            verify(questionRepository).findById(1L);
            verify(answerRepository).save(any(Answer.class));
            verify(questionRepository).save(question);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when session not found")
        void shouldThrowExceptionWhenSessionNotFound() {
            // Arrange
            when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () ->
                    aiInterviewService.processAnswer(1L, 999L, 1L, "Answer"));

            // Verify interactions
            verify(sessionRepository).findById(999L);
            verify(questionRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when question not found")
        void shouldThrowExceptionWhenQuestionNotFound() {
            // Arrange
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(questionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () ->
                    aiInterviewService.processAnswer(1L, 1L, 999L, "Answer"));

            // Verify interactions
            verify(sessionRepository).findById(1L);
            verify(questionRepository).findById(999L);
            verify(answerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Generate Next Question Tests")
    class GenerateNextQuestionTests {

        @Test
        @DisplayName("Should generate next question successfully")
        void shouldGenerateNextQuestionSuccessfully() {
            // Arrange
            AIServiceDefault.QuestionResponse questionResponse = new AIServiceDefault.QuestionResponse();
            questionResponse.setQuestion("What are the principles of SOLID?");
            questionResponse.setExpectedAnswer("SOLID is an acronym for five design principles in object-oriented programming...");

            when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(aiService.generateQuestion(anyString(), anyString(), anyString())).thenReturn(questionResponse);
            when(questionRepository.save(any(Question.class))).thenReturn(question);
            when(sessionRepository.save(any(InterviewSession.class))).thenReturn(session);
            when(questionMapper.toDTO(any(Question.class))).thenReturn(questionDTO);

            // Act
            QuestionDTO result = aiInterviewService.generateNextQuestion(1L, 1L);

            // Assert
            assertNotNull(result);
            assertEquals(questionDTO.getId(), result.getId());
            assertEquals(questionDTO.getContent(), result.getContent());

            // Verify interactions
            verify(sessionRepository).findById(1L);
            verify(aiService).generateQuestion(session.getPosition(), session.getExperienceLevel(), session.getInterviewContext());
            verify(questionRepository).save(any(Question.class));
            verify(sessionRepository).save(session);
            verify(questionMapper).toDTO(any(Question.class));
        }

    }}