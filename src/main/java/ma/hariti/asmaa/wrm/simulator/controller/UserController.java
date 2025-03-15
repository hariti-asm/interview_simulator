package ma.hariti.asmaa.wrm.simulator.controller;

import ma.hariti.asmaa.wrm.simulator.dto.request.InterviewSessionDTO;
import ma.hariti.asmaa.wrm.simulator.dto.request.UserDTO;
import ma.hariti.asmaa.wrm.simulator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        System.out.println(userDTO.toString());
        UserDTO createdUser = userService.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        UserDTO updatedUser = userService.updateUser(userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/interviews")
    public ResponseEntity<List<InterviewSessionDTO>> getUserInterviews(@PathVariable Long id) {
        List<InterviewSessionDTO> interviews = userService.getUserInterviews(id);
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/{id}/performance")
    public ResponseEntity<Object> getUserPerformance(@PathVariable Long id) {
        Object performance = userService.getUserPerformance(id);
        return ResponseEntity.ok(performance);
    }
}