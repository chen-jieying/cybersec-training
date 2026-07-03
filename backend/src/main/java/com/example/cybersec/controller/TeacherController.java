package com.example.cybersec.controller;

import com.example.cybersec.model.Question;
import com.example.cybersec.model.SchoolClass;
import com.example.cybersec.model.ScenarioScript;
import com.example.cybersec.model.TrainingRecord;
import com.example.cybersec.model.User;
import com.example.cybersec.repository.QuestionRepository;
import com.example.cybersec.repository.SchoolClassRepository;
import com.example.cybersec.repository.ScenarioScriptRepository;
import com.example.cybersec.repository.TrainingRecordRepository;
import com.example.cybersec.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

  private final UserRepository userRepository;
  private final SchoolClassRepository schoolClassRepository;
  private final QuestionRepository questionRepository;
  private final ScenarioScriptRepository scenarioScriptRepository;
  private final TrainingRecordRepository trainingRecordRepository;

  public TeacherController(UserRepository userRepository,
                           SchoolClassRepository schoolClassRepository,
                           QuestionRepository questionRepository,
                           ScenarioScriptRepository scenarioScriptRepository,
                           TrainingRecordRepository trainingRecordRepository) {
    this.userRepository = userRepository;
    this.schoolClassRepository = schoolClassRepository;
    this.questionRepository = questionRepository;
    this.scenarioScriptRepository = scenarioScriptRepository;
    this.trainingRecordRepository = trainingRecordRepository;
  }

  @GetMapping("/classes")
  public List<SchoolClass> getAllClasses() {
    return schoolClassRepository.findAll();
  }

  @GetMapping("/students")
  public List<User> getAllStudents() {
    return userRepository.findAll().stream()
        .filter((User user) -> "student".equalsIgnoreCase(user.getRole()))
        .collect(Collectors.toList());
  }

  @PostMapping("/questions")
  public Question addQuestion(@RequestBody Question question) {
    return questionRepository.save(question);
  }

  @GetMapping("/scenarios")
  public List<ScenarioScript> getScenarios() {
    return scenarioScriptRepository.findAll();
  }

  @GetMapping("/records")
  public List<TrainingRecord> getTrainingRecords(@RequestParam(required = false) Long studentId) {
    if (studentId != null) {
      return trainingRecordRepository.findByStudentId(studentId);
    }
    return trainingRecordRepository.findAll();
  }

  @PostMapping("/records")
  public TrainingRecord saveTrainingRecord(@RequestBody TrainingRecord record) {
    return trainingRecordRepository.save(record);
  }

  @GetMapping("/me")
  public ResponseEntity<?> getTeacherProfile(@RequestHeader("X-User-Name") String username) {
    Optional<User> teacher = userRepository.findByUsername(username);
    if (teacher.isPresent()) {
      return ResponseEntity.ok(teacher.get());
    }
    return ResponseEntity.badRequest().body("Unable to load teacher profile");
  }
}

