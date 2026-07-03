package com.example.cybersec.controller;

import com.example.cybersec.model.ScenarioScript;
import com.example.cybersec.model.TrainingRecord;
import com.example.cybersec.model.User;
import com.example.cybersec.repository.ScenarioScriptRepository;
import com.example.cybersec.repository.TrainingRecordRepository;
import com.example.cybersec.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/student")
public class StudentController {

  private final UserRepository userRepository;
  private final ScenarioScriptRepository scenarioScriptRepository;
  private final TrainingRecordRepository trainingRecordRepository;

  public StudentController(UserRepository userRepository,
                           ScenarioScriptRepository scenarioScriptRepository,
                           TrainingRecordRepository trainingRecordRepository) {
    this.userRepository = userRepository;
    this.scenarioScriptRepository = scenarioScriptRepository;
    this.trainingRecordRepository = trainingRecordRepository;
  }

  @GetMapping("/profile")
  public ResponseEntity<?> getProfile(@RequestHeader("X-User-Name") String username) {
    Optional<User> student = userRepository.findByUsername(username);
    if (student.isPresent()) {
      return ResponseEntity.ok(student.get());
    }
    return ResponseEntity.badRequest().body("Unable to load student profile");
  }

  @GetMapping("/scenarios")
  public List<ScenarioScript> getScenarios() {
    return scenarioScriptRepository.findAll();
  }

  @GetMapping("/records")
  public List<TrainingRecord> getRecords(@RequestHeader("X-User-Name") String username) {
    return userRepository.findByUsername(username)
        .map((User user) -> trainingRecordRepository.findByStudentId(user.getId()))
        .orElse(Collections.emptyList());
  }

  @PostMapping("/submit")
  public ResponseEntity<?> submitTraining(@RequestBody TrainingRecord record,
                                          @RequestHeader("X-User-Name") String username) {
    Optional<User> student = userRepository.findByUsername(username);
    if (student.isEmpty()) {
      return ResponseEntity.badRequest().body("Student not found");
    }

    record.setStudentId(student.get().getId());
    record.setCompletedAt(LocalDateTime.now().toString());
    TrainingRecord saved = trainingRecordRepository.save(record);
    return ResponseEntity.ok(saved);
  }
}

