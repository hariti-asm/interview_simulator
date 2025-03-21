package ma.hariti.asmaa.wrm.simulator;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.UserDTO;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.entity.User;
import ma.hariti.asmaa.wrm.simulator.mapper.InterviewSessionMapper;
import ma.hariti.asmaa.wrm.simulator.mapper.UserMapper;
import ma.hariti.asmaa.wrm.simulator.repository.UserRepository;
import ma.hariti.asmaa.wrm.simulator.service.serviceDefault.UserServiceDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private InterviewSessionMapper interviewSessionMapper;

    @InjectMocks
    private UserServiceDefault userService;

    private UserDTO userDTO;
    private User user;
    private List<User> userList;
    private List<UserDTO> userDTOList;
    private InterviewSession interviewSession;
    private List<InterviewSession> sessionList;
    private List<InterviewSessionDTO> sessionDTOList;

    @BeforeEach
    void setUp() {
        // Initialize test data
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("John");
        userDTO.setEmail("john.doe@example.com");
        userDTO.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john.doe@example.com");
        user.setPassword("encodedPassword");

        userList = new ArrayList<>();
        userList.add(user);

        userDTOList = new ArrayList<>();
        userDTOList.add(userDTO);

        interviewSession = new InterviewSession();
        interviewSession.setId(1L);

        sessionList = new ArrayList<>();
        sessionList.add(interviewSession);

        sessionDTOList = new ArrayList<>();
        InterviewSessionDTO sessionDTO = new InterviewSessionDTO();
        sessionDTO.setId(1L);
        sessionDTOList.add(sessionDTO);
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() {
            // Arrange
            when(userMapper.toEntity(userDTO)).thenReturn(user);
            when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(userDTO);

            // Act
            UserDTO result = userService.createUser(userDTO);

            // Assert
            assertNotNull(result);
            assertEquals(userDTO.getId(), result.getId());
            assertEquals(userDTO.getEmail(), result.getEmail());

            // Verify interactions
            verify(userMapper).toEntity(userDTO);
            verify(passwordEncoder).encode(userDTO.getPassword());
            verify(userRepository).save(user);
            verify(userMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should handle null input")
        void shouldHandleNullInput() {
            UserDTO nullDTO = null;
            when(userMapper.toEntity(nullDTO)).thenThrow(new NullPointerException());

            assertThrows(NullPointerException.class, () -> userService.createUser(nullDTO));
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(userDTO);

            // Act
            UserDTO result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals(userDTO.getId(), result.getId());
            assertEquals(userDTO.getEmail(), result.getEmail());

            verify(userRepository).findById(1L);
            verify(userMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> userService.getUserById(999L));

            verify(userRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should get all users successfully")
        void shouldGetAllUsersSuccessfully() {
            // Arrange
            when(userRepository.findAll()).thenReturn(userList);
            when(userMapper.toDTOList(userList)).thenReturn(userDTOList);

            // Act
            List<UserDTO> result = userService.getAllUsers();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(userDTO.getId(), result.get(0).getId());

            // Verify interactions
            verify(userRepository).findAll();
            verify(userMapper).toDTOList(userList);
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll()).thenReturn(new ArrayList<>());
            when(userMapper.toDTOList(any())).thenReturn(new ArrayList<>());

            List<UserDTO> result = userService.getAllUsers();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            doNothing().when(userMapper).updateUserFromDTO(userDTO, user);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(userDTO);

            UserDTO result = userService.updateUser(userDTO);

            assertNotNull(result);
            assertEquals(userDTO.getId(), result.getId());

            verify(userRepository).findById(1L);
            verify(userMapper).updateUserFromDTO(userDTO, user);
            verify(userRepository).save(user);
            verify(userMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should update user with new password")
        void shouldUpdateUserWithNewPassword() {
            userDTO.setPassword("newPassword123");

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            doNothing().when(userMapper).updateUserFromDTO(userDTO, user);
            when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDTO(user)).thenReturn(userDTO);

            UserDTO result = userService.updateUser(userDTO);

            assertNotNull(result);

            verify(userRepository).findById(1L);
            verify(userMapper).updateUserFromDTO(userDTO, user);
            verify(passwordEncoder).encode("newPassword123");
            verify(userRepository).save(user);
            verify(userMapper).toDTO(user);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when updating non-existent user")
        void shouldThrowExceptionWhenUpdatingNonExistentUser() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            UserDTO nonExistentUserDTO = new UserDTO();
            nonExistentUserDTO.setId(999L);

            assertThrows(EntityNotFoundException.class, () -> userService.updateUser(nonExistentUserDTO));

            verify(userRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            doNothing().when(userRepository).delete(user);

            userService.deleteUser(1L);

            verify(userRepository).findById(1L);
            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when deleting non-existent user")
        void shouldThrowExceptionWhenDeletingNonExistentUser() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(999L));

            verify(userRepository).findById(999L);
            verify(userRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Get User Interviews Tests")
    class GetUserInterviewsTests {
        @Nested
        @DisplayName("Authenticate User Tests")
        class AuthenticateUserTests {

            @Test
            @DisplayName("Should authenticate user successfully")
            void shouldAuthenticateUserSuccessfully() {
                String username = "john.doe@example.com";
                String password = "password123";

                when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
                when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

                boolean result = userService.authenticateUser(username, password);

                assertTrue(result);

                verify(userRepository).findByEmail(username);
                verify(passwordEncoder).matches(password, user.getPassword());
            }

            @Test
            @DisplayName("Should fail authentication with incorrect password")
            void shouldFailAuthenticationWithIncorrectPassword() {
                String username = "john.doe@example.com";
                String wrongPassword = "wrongPassword";

                when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
                when(passwordEncoder.matches(wrongPassword, user.getPassword())).thenReturn(false);

                boolean result = userService.authenticateUser(username, wrongPassword);

                assertFalse(result);

                verify(userRepository).findByEmail(username);
                verify(passwordEncoder).matches(wrongPassword, user.getPassword());
            }

            @Test
            @DisplayName("Should fail authentication with non-existent user")
            void shouldFailAuthenticationWithNonExistentUser() {
                String nonExistentUsername = "nonexistent@example.com";
                String password = "password123";

                when(userRepository.findByEmail(nonExistentUsername)).thenReturn(Optional.empty());

                boolean result = userService.authenticateUser(nonExistentUsername, password);

                assertFalse(result);

                verify(userRepository).findByEmail(nonExistentUsername);
                verify(passwordEncoder, never()).matches(any(), any());
            }
        }
    }
}