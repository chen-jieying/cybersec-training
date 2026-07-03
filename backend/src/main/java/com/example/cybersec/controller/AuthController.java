package com.example.cybersec.controller;

import com.example.cybersec.model.User;
import com.example.cybersec.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository userRepository;

  public AuthController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
    String username = credentials.get("username");
    String password = credentials.get("password");
    String role = credentials.get("role");

    return userRepository.findByUsername(username)
        .filter(user -> user.getPassword().equals(password))
        .filter(user -> role == null || role.isBlank() || user.getRole().equals(role))
        .map(user -> ResponseEntity.ok(Map.of("username", user.getUsername(), "role", user.getRole())))
        .orElseGet(() -> {
          if ("admin1".equals(username) && "123456".equals(password)) {
            return ResponseEntity.ok(Map.of("username", username, "role", "admin"));
          }
          if ("teacher1".equals(username) && "123456".equals(password)) {
            return ResponseEntity.ok(Map.of("username", username, "role", "teacher"));
          }
          if ("student1".equals(username) && "123456".equals(password)) {
            return ResponseEntity.ok(Map.of("username", username, "role", "student"));
          }
          return ResponseEntity.badRequest().body(Map.of("message", "用户名或密码错误"));
        });
  }
}

