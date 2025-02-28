package ma.hariti.asmaa.wrm.simulator.service;

import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    UserDTO createUser(UserDTO userDTO);

    UserDTO getUserById(Long id);

    List<UserDTO> getAllUsers();

    UserDTO updateUser(UserDTO userDTO);

    void deleteUser(Long id);

    List<InterviewSessionDTO> getUserInterviews(Long userId);

    Object getUserPerformance(Long userId);

    boolean authenticateUser(String username, String password);


}