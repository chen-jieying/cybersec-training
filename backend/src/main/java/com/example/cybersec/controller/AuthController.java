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
        .map(user -> {
          Map<String, Object> result = new java.util.HashMap<>();
          result.put("id", user.getId());
          result.put("username", user.getUsername());
          result.put("role", user.getRole());
          result.put("fullName", user.getFullName());
          result.put("grade", user.getGrade());
          result.put("classId", user.getClassId());
          result.put("title", user.getTitle());
          result.put("phone", user.getPhone());
          return ResponseEntity.ok((Object) result);
        })
        .orElse(ResponseEntity.badRequest().body(Map.of("message", "用户名或密码错误")));
  }
}

