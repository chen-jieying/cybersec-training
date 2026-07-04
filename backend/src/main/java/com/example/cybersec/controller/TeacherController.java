package com.example.cybersec.controller;

import com.example.cybersec.model.BehaviorRecord;
import com.example.cybersec.model.Question;
import com.example.cybersec.model.SchoolClass;
import com.example.cybersec.model.ScenarioScript;
import com.example.cybersec.model.TrainingRecord;
import com.example.cybersec.model.User;
import com.example.cybersec.repository.BehaviorRecordRepository;
import com.example.cybersec.repository.QuestionRepository;
import com.example.cybersec.repository.SchoolClassRepository;
import com.example.cybersec.repository.ScenarioScriptRepository;
import com.example.cybersec.repository.TrainingRecordRepository;
import com.example.cybersec.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

  private final UserRepository userRepository;
  private final SchoolClassRepository schoolClassRepository;
  private final QuestionRepository questionRepository;
  private final ScenarioScriptRepository scenarioScriptRepository;
  private final TrainingRecordRepository trainingRecordRepository;
  private final BehaviorRecordRepository behaviorRecordRepository;

  public TeacherController(UserRepository userRepository,
                           SchoolClassRepository schoolClassRepository,
                           QuestionRepository questionRepository,
                           ScenarioScriptRepository scenarioScriptRepository,
                           TrainingRecordRepository trainingRecordRepository,
                           BehaviorRecordRepository behaviorRecordRepository) {
    this.userRepository = userRepository;
    this.schoolClassRepository = schoolClassRepository;
    this.questionRepository = questionRepository;
    this.scenarioScriptRepository = scenarioScriptRepository;
    this.trainingRecordRepository = trainingRecordRepository;
    this.behaviorRecordRepository = behaviorRecordRepository;
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

  /**
   * 获取指定班级的学生列表
   */
  @GetMapping("/students/class/{classId}")
  public ResponseEntity<?> getStudentsByClass(@PathVariable Long classId) {
    List<User> students = userRepository.findAll().stream()
        .filter(u -> "student".equalsIgnoreCase(u.getRole()) && classId.equals(u.getClassId()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(students);
  }

  /**
   * 获取某个学生的对话实训记录
   */
  @GetMapping("/chat-records/{studentId}")
  public ResponseEntity<?> getStudentChatRecords(@PathVariable Long studentId) {
    Optional<User> student = userRepository.findById(studentId);
    if (student.isEmpty() || !"student".equalsIgnoreCase(student.get().getRole())) {
      return ResponseEntity.badRequest().body(Map.of("error", "学生不存在"));
    }

    // 获取学生的行为记录（对话实训记录）
    List<BehaviorRecord> behaviors = behaviorRecordRepository.findByUserId(studentId);
    // 获取学生的训练记录
    List<TrainingRecord> trainingRecords = trainingRecordRepository.findByStudentId(studentId);

    Map<String, Object> result = new HashMap<>();
    result.put("student", student.get());
    result.put("chatRecords", behaviors.stream()
        .filter(b -> "chat".equalsIgnoreCase(b.getActionType()))
        .collect(Collectors.toList()));
    result.put("trainingRecords", trainingRecords);

    return ResponseEntity.ok(result);
  }

  /**
   * 获取全班学生的对话实训汇总
   */
  @GetMapping("/class-chat-summary/{classId}")
  public ResponseEntity<?> getClassChatSummary(@PathVariable Long classId) {
    List<User> students = userRepository.findAll().stream()
        .filter(u -> "student".equalsIgnoreCase(u.getRole()) && classId.equals(u.getClassId()))
        .collect(Collectors.toList());

    List<Map<String, Object>> summary = new ArrayList<>();
    for (User student : students) {
      List<TrainingRecord> records = trainingRecordRepository.findByStudentId(student.getId());
      List<BehaviorRecord> chatRecords = behaviorRecordRepository.findByUserId(student.getId()).stream()
          .filter(b -> "chat".equalsIgnoreCase(b.getActionType()))
          .collect(Collectors.toList());

      Map<String, Object> studentSummary = new HashMap<>();
      studentSummary.put("studentId", student.getId());
      studentSummary.put("studentName", student.getFullName());
      studentSummary.put("className", student.getGrade());
      studentSummary.put("completedScenarios", records.size());
      studentSummary.put("avgScore", records.isEmpty() ? 0 :
          records.stream().mapToInt(TrainingRecord::getScore).average().orElse(0));
      studentSummary.put("chatCount", chatRecords.size());
      summary.add(studentSummary);
    }

    return ResponseEntity.ok(summary);
  }

  @PostMapping("/questions")
  public Question addQuestion(@RequestBody Question question) {
    return questionRepository.save(question);
  }

  /** 更新题目（含解析字段） */
  @PutMapping("/questions/{id}")
  public ResponseEntity<?> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
    Optional<Question> existing = questionRepository.findById(id);
    if (existing.isEmpty()) return ResponseEntity.notFound().build();
    question.setId(id);
    return ResponseEntity.ok(questionRepository.save(question));
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

