package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;



import jakarta.persistence.EntityNotFoundException;
import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.UserDTO;
import ma.hariti.asmaa.wrm.simulator.entity.InterviewSession;
import ma.hariti.asmaa.wrm.simulator.mapper.InterviewSessionMapper;
import ma.hariti.asmaa.wrm.simulator.repository.UserRepository;
import ma.hariti.asmaa.wrm.simulator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


import ma.hariti.asmaa.wrm.simulator.entity.User;
import ma.hariti.asmaa.wrm.simulator.mapper.UserMapper;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final InterviewSessionMapper interviewSessionMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, InterviewSessionMapper interviewSessionMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.interviewSessionMapper = interviewSessionMapper;
    }


    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toDTOList(users);
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        User existingUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userDTO.getId()));

        userMapper.updateUserFromDTO(userDTO, existingUser);

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    public List<InterviewSessionDTO> getUserInterviews(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<InterviewSession> sessions = user.getSessions();
        return interviewSessionMapper.toDTOList(sessions);
    }

    @Override
    public Object getUserPerformance(Long userId) {

        return null;
    }

    @Override
    public boolean authenticateUser(String username, String password) {
        User user = userRepository.findByEmail(username)
                .orElse(null);

        if (user != null) {
            return passwordEncoder.matches(password, user.getPassword());
        }

        return false;
    }

}