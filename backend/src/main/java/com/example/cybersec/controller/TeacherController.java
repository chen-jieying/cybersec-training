package com.example.cybersec.controller;

import com.example.cybersec.model.*;
import com.example.cybersec.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师端控制器 - 班级管理、学生答题详情查看
 * 所有数据从数据库真实读取，无虚拟数据
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

  private final UserRepository userRepository;
  private final SchoolClassRepository schoolClassRepository;
  private final QuestionRepository questionRepository;
  private final ScenarioScriptRepository scenarioScriptRepository;
  private final TrainingRecordRepository trainingRecordRepository;
  private final BehaviorRecordRepository behaviorRecordRepository;
  private final ExamAnswerRepository examAnswerRepository;

  public TeacherController(UserRepository userRepository,
                           SchoolClassRepository schoolClassRepository,
                           QuestionRepository questionRepository,
                           ScenarioScriptRepository scenarioScriptRepository,
                           TrainingRecordRepository trainingRecordRepository,
                           BehaviorRecordRepository behaviorRecordRepository,
                           ExamAnswerRepository examAnswerRepository) {
    this.userRepository = userRepository;
    this.schoolClassRepository = schoolClassRepository;
    this.questionRepository = questionRepository;
    this.scenarioScriptRepository = scenarioScriptRepository;
    this.trainingRecordRepository = trainingRecordRepository;
    this.behaviorRecordRepository = behaviorRecordRepository;
    this.examAnswerRepository = examAnswerRepository;
  }

  // ==================== 班级管理 ====================

  /** 获取当前教师名下的班级列表（按 teacherId 匹配） */
  @GetMapping("/classes")
  public ResponseEntity<?> getMyClasses(@RequestHeader("X-User-Name") String username) {
    Optional<User> teacher = userRepository.findByUsername(username);
    if (teacher.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "教师不存在"));
    }

    Long teacherId = teacher.get().getId();
    List<SchoolClass> classes = schoolClassRepository.findByTeacherId(teacherId);

    // 统计每个班级的学生数
    List<Map<String, Object>> result = classes.stream().map(cls -> {
      long studentCount = userRepository.findAll().stream()
          .filter(u -> "student".equalsIgnoreCase(u.getRole()) && cls.getId().equals(u.getClassId()))
          .count();

      Map<String, Object> map = new HashMap<>();
      map.put("id", cls.getId());
      map.put("grade", cls.getGrade());
      map.put("className", cls.getClassName());
      map.put("teacherId", cls.getTeacherId());
      map.put("teacherName", cls.getTeacherName());
      map.put("studentCount", (int) studentCount);
      return map;
    }).collect(Collectors.toList());

    return ResponseEntity.ok(result);
  }

  /** 获取全部班级（管理员视角） */
  @GetMapping("/all-classes")
  public List<SchoolClass> getAllClasses() {
    return schoolClassRepository.findAll();
  }

  // ==================== 学生管理 ====================

  @GetMapping("/students")
  public List<User> getAllStudents() {
    return userRepository.findAll().stream()
        .filter(u -> "student".equalsIgnoreCase(u.getRole()))
        .collect(Collectors.toList());
  }

  /** 获取指定班级的学生列表（含真实考试数据） */
  @GetMapping("/classes/{classId}/students")
  public ResponseEntity<?> getClassStudents(@PathVariable Long classId) {
    List<User> students = userRepository.findAll().stream()
        .filter(u -> "student".equalsIgnoreCase(u.getRole()) && classId.equals(u.getClassId()))
        .collect(Collectors.toList());

    List<Map<String, Object>> result = new ArrayList<>();
    for (User s : students) {
      List<ExamAnswer> answers = examAnswerRepository.findByStudentId(s.getId());
      List<TrainingRecord> records = trainingRecordRepository.findByStudentId(s.getId());

      // 计算平均分
      double avgScore = 0;
      if (!answers.isEmpty()) {
        // 按考试会话分组计算每次的得分
        Map<String, Integer> sessionScores = new LinkedHashMap<>();
        for (ExamAnswer a : answers) {
          sessionScores.merge(a.getExamSessionId(),
              a.getScore() != null ? a.getScore() : 0, Integer::sum);
        }
        avgScore = sessionScores.values().stream().mapToInt(Integer::intValue).average().orElse(0);
      }

      Map<String, Object> studentInfo = new HashMap<>();
      studentInfo.put("id", s.getId());
      studentInfo.put("username", s.getUsername());
      studentInfo.put("fullName", s.getFullName());
      studentInfo.put("grade", s.getGrade());
      studentInfo.put("classId", s.getClassId());
      studentInfo.put("avgScore", Math.round(avgScore * 10) / 10.0);
      studentInfo.put("examCount", answers.isEmpty() ? 0 :
          (int) answers.stream().map(ExamAnswer::getExamSessionId).distinct().count());
      studentInfo.put("totalQuestions", answers.size());
      studentInfo.put("correctQuestions", answers.stream().filter(ExamAnswer::getIsCorrect).count());
      result.add(studentInfo);
    }

    return ResponseEntity.ok(result);
  }

  // ==================== 班级统计 ====================

  /** 获取班级统计数据 */
  @GetMapping("/classes/{classId}/stats")
  public ResponseEntity<?> getClassStats(@PathVariable Long classId) {
    List<User> students = userRepository.findAll().stream()
        .filter(u -> "student".equalsIgnoreCase(u.getRole()) && classId.equals(u.getClassId()))
        .collect(Collectors.toList());

    if (students.isEmpty()) {
      Map<String, Object> empty = new HashMap<>();
      empty.put("classId", classId);
      empty.put("totalStudents", 0);
      empty.put("avgScore", 0);
      empty.put("passRate", 0);
      empty.put("completionRate", 0);
      empty.put("students", Collections.emptyList());
      return ResponseEntity.ok(empty);
    }

    // 计算班级统计
    int totalStudents = students.size();
    double totalAvg = 0;
    int passedCount = 0;
    int completedCount = 0;
    int totalExamCount = 0;

    for (User s : students) {
      List<ExamAnswer> answers = examAnswerRepository.findByStudentId(s.getId());
      if (!answers.isEmpty()) completedCount++;
      totalExamCount += answers.size();

      Map<String, Integer> sessionScores = new LinkedHashMap<>();
      for (ExamAnswer a : answers) {
        sessionScores.merge(a.getExamSessionId(),
            a.getScore() != null ? a.getScore() : 0, Integer::sum);
      }
      double studentAvg = sessionScores.values().stream()
          .mapToInt(Integer::intValue).average().orElse(0);
      totalAvg += studentAvg;
      if (studentAvg >= 60) passedCount++;
    }

    double classAvg = totalStudents > 0 ? Math.round(totalAvg / totalStudents * 10) / 10.0 : 0;
    double passRate = totalStudents > 0 ? Math.round((double) passedCount / totalStudents * 1000) / 10.0 : 0;
    double completionRate = totalStudents > 0 ? Math.round((double) completedCount / totalStudents * 1000) / 10.0 : 0;

    Map<String, Object> stats = new HashMap<>();
    stats.put("classId", classId);
    stats.put("totalStudents", totalStudents);
    stats.put("avgScore", classAvg);
    stats.put("passRate", passRate);
    stats.put("completionRate", completionRate);
    stats.put("totalExamCount", totalExamCount);

    return ResponseEntity.ok(stats);
  }

  // ==================== 学生答题详情 ====================

  /** 查看某学生的详细答题记录（每题选了什么） */
  @GetMapping("/student/{studentId}/exam-detail")
  public ResponseEntity<?> getStudentExamDetail(@PathVariable Long studentId) {
    Optional<User> student = userRepository.findById(studentId);
    if (student.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "学生不存在"));
    }

    List<ExamAnswer> allAnswers = examAnswerRepository.findByStudentId(studentId);

    // 按考试会话分组
    Map<String, List<Map<String, Object>>> sessionMap = new LinkedHashMap<>();
    for (ExamAnswer a : allAnswers) {
      questionRepository.findById(a.getQuestionId()).ifPresent(q -> {
        Map<String, Object> item = new HashMap<>();
        item.put("questionId", q.getId());
        item.put("questionText", q.getQuestionText());
        item.put("optionA", q.getOptionA());
        item.put("optionB", q.getOptionB());
        item.put("optionC", q.getOptionC());
        item.put("optionD", q.getOptionD());
        item.put("userAnswer", a.getSelectedOption());
        item.put("correctAnswer", a.getCorrectAnswer());
        item.put("isCorrect", a.getIsCorrect());
        item.put("score", a.getScore());
        item.put("category", a.getCategory());
        item.put("explanation", q.getExplanation());
        item.put("answeredAt", a.getAnsweredAt());

        sessionMap.computeIfAbsent(a.getExamSessionId(), k -> new ArrayList<>()).add(item);
      });
    }

    // 构建每次考试的信息
    List<Map<String, Object>> examSessions = new ArrayList<>();
    for (Map.Entry<String, List<Map<String, Object>>> entry : sessionMap.entrySet()) {
      List<Map<String, Object>> items = entry.getValue();
      int sessionScore = items.stream().mapToInt(i -> (int) i.getOrDefault("score", 0)).sum();
      long correctCount = items.stream().filter(i -> Boolean.TRUE.equals(i.get("isCorrect"))).count();

      Map<String, Object> session = new HashMap<>();
      session.put("sessionId", entry.getKey());
      session.put("score", sessionScore);
      session.put("totalCount", items.size());
      session.put("correctCount", correctCount);
      session.put("stageId", allAnswers.stream()
          .filter(a -> entry.getKey().equals(a.getExamSessionId()))
          .findFirst().map(ExamAnswer::getStageId).orElse(null));
      session.put("answeredAt", items.isEmpty() ? "" : items.get(0).get("answeredAt"));
      session.put("questions", items);
      examSessions.add(session);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("student", student.get());
    result.put("examSessions", examSessions);
    result.put("totalExams", examSessions.size());
    result.put("totalQuestions", allAnswers.size());
    result.put("correctQuestions", allAnswers.stream().filter(ExamAnswer::getIsCorrect).count());

    return ResponseEntity.ok(result);
  }

  // ==================== 对话实训 ====================

  /** 获取某个学生的对话实训记录 */
  @GetMapping("/chat-records/{studentId}")
  public ResponseEntity<?> getStudentChatRecords(@PathVariable Long studentId) {
    Optional<User> student = userRepository.findById(studentId);
    if (student.isEmpty() || !"student".equalsIgnoreCase(student.get().getRole())) {
      return ResponseEntity.badRequest().body(Map.of("error", "学生不存在"));
    }

    List<BehaviorRecord> behaviors = behaviorRecordRepository.findByUserId(studentId);
    List<TrainingRecord> trainingRecords = trainingRecordRepository.findByStudentId(studentId);

    Map<String, Object> result = new HashMap<>();
    result.put("student", student.get());
    result.put("chatRecords", behaviors.stream()
        .filter(b -> "chat".equalsIgnoreCase(b.getActionType()))
        .collect(Collectors.toList()));
    result.put("trainingRecords", trainingRecords);

    return ResponseEntity.ok(result);
  }

  /** 获取全班学生的对话实训汇总 */
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
          records.stream().mapToInt(r -> r.getScore() != null ? r.getScore() : 0).average().orElse(0));
      studentSummary.put("chatCount", chatRecords.size());
      summary.add(studentSummary);
    }

    return ResponseEntity.ok(summary);
  }

  // ==================== 题目管理 ====================

  @PostMapping("/questions")
  public Question addQuestion(@RequestBody Question question) {
    return questionRepository.save(question);
  }

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
