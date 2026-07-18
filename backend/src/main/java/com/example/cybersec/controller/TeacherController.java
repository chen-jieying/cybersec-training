package com.example.cybersec.controller;

import com.example.cybersec.model.*;
import com.example.cybersec.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
  private final TrainingTaskRepository trainingTaskRepository;

  public TeacherController(UserRepository userRepository,
                           SchoolClassRepository schoolClassRepository,
                           QuestionRepository questionRepository,
                           ScenarioScriptRepository scenarioScriptRepository,
                           TrainingRecordRepository trainingRecordRepository,
                           BehaviorRecordRepository behaviorRecordRepository,
                           ExamAnswerRepository examAnswerRepository,
                           TrainingTaskRepository trainingTaskRepository) {
    this.userRepository = userRepository;
    this.schoolClassRepository = schoolClassRepository;
    this.questionRepository = questionRepository;
    this.scenarioScriptRepository = scenarioScriptRepository;
    this.trainingRecordRepository = trainingRecordRepository;
    this.behaviorRecordRepository = behaviorRecordRepository;
    this.examAnswerRepository = examAnswerRepository;
    this.trainingTaskRepository = trainingTaskRepository;
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

  /** 创建班级 */
  @PostMapping("/classes")
  public SchoolClass createClass(@RequestBody SchoolClass schoolClass) {
    return schoolClassRepository.save(schoolClass);
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

  /** 获取当前教师名下所有班级的学生总数 */
  @GetMapping("/total-students")
  public ResponseEntity<?> getTeacherTotalStudents(@RequestHeader("X-User-Name") String username) {
    Optional<User> teacher = userRepository.findByUsername(username);
    if (teacher.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "教师不存在"));
    }
    Long teacherId = teacher.get().getId();
    List<SchoolClass> classes = schoolClassRepository.findByTeacherId(teacherId);
    int totalStudents = 0;
    for (SchoolClass cls : classes) {
      long count = userRepository.findAll().stream()
          .filter(u -> "student".equalsIgnoreCase(u.getRole()) && cls.getId().equals(u.getClassId()))
          .count();
      totalStudents += (int) count;
    }
    return ResponseEntity.ok(Map.of("totalStudents", totalStudents, "totalClasses", classes.size()));
  }

  /** 获取学生对话实训的详细消息记录（每条行为记录） */
  @GetMapping("/chat-messages/{studentId}")
  public ResponseEntity<?> getStudentChatMessages(@PathVariable Long studentId) {
    Optional<User> student = userRepository.findById(studentId);
    if (student.isEmpty() || !"student".equalsIgnoreCase(student.get().getRole())) {
      return ResponseEntity.badRequest().body(Map.of("error", "学生不存在"));
    }

    List<BehaviorRecord> allBehaviors = behaviorRecordRepository.findByUserId(studentId);

    // 对话消息记录（actionType = "chat" 或 "message"）
    List<BehaviorRecord> chatMessages = allBehaviors.stream()
        .filter(b -> "chat".equalsIgnoreCase(b.getActionType())
            || "message".equalsIgnoreCase(b.getActionType())
            || "reply".equalsIgnoreCase(b.getActionType()))
        .collect(Collectors.toList());

    // 训练记录
    List<TrainingRecord> trainingRecords = trainingRecordRepository.findByStudentId(studentId);

    Map<String, Object> result = new HashMap<>();
    result.put("studentId", studentId);
    result.put("studentName", student.get().getFullName());
    result.put("messages", chatMessages);
    result.put("trainingRecords", trainingRecords);
    result.put("totalMessages", chatMessages.size());
    result.put("totalTrainings", trainingRecords.size());

    return ResponseEntity.ok(result);
  }

  /** 获取学生资源学习记录 */
  @GetMapping("/resource-records/{studentId}")
  public ResponseEntity<?> getStudentResourceRecords(@PathVariable Long studentId) {
    List<BehaviorRecord> records = behaviorRecordRepository.findByUserId(studentId);
    List<BehaviorRecord> resourceRecords = records.stream()
        .filter(b -> "resource_view".equalsIgnoreCase(b.getActionType())
            || "resource_download".equalsIgnoreCase(b.getActionType())
            || "resource_preview".equalsIgnoreCase(b.getActionType()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(resourceRecords);
  }

  @GetMapping("/me")
  public ResponseEntity<?> getTeacherProfile(@RequestHeader("X-User-Name") String username) {
    Optional<User> teacher = userRepository.findByUsername(username);
    if (teacher.isPresent()) {
      return ResponseEntity.ok(teacher.get());
    }
    return ResponseEntity.badRequest().body("Unable to load teacher profile");
  }

  // ==================== 实训任务管理 ====================

  /** 获取某班级的所有实训任务 */
  @GetMapping("/tasks")
  public ResponseEntity<?> getClassTasks(@RequestParam Long classId) {
    List<TrainingTask> tasks = trainingTaskRepository.findByClassId(classId);
    // 附加场景信息
    List<Map<String, Object>> result = tasks.stream().map(task -> {
      Map<String, Object> map = new HashMap<>();
      map.put("id", task.getId());
      map.put("classId", task.getClassId());
      map.put("scenarioId", task.getScenarioId());
      map.put("title", task.getTitle());
      map.put("description", task.getDescription());
      map.put("teacherId", task.getTeacherId());
      map.put("teacherName", task.getTeacherName());
      map.put("createdAt", task.getCreatedAt());
      // 统计完成人数
      List<User> classStudents = userRepository.findAll().stream()
          .filter(u -> "student".equalsIgnoreCase(u.getRole()) && task.getClassId().equals(u.getClassId()))
          .collect(Collectors.toList());
      long completedCount = classStudents.stream()
          .filter(s -> trainingRecordRepository.findByStudentId(s.getId()).stream()
              .anyMatch(r -> task.getScenarioId().equals(r.getScenarioId())))
          .count();
      map.put("studentCount", classStudents.size());
      map.put("completedCount", (int) completedCount);
      return map;
    }).collect(Collectors.toList());
    return ResponseEntity.ok(result);
  }

  /** 发布实训任务到班级 */
  @PostMapping("/tasks")
  public ResponseEntity<?> createTask(@RequestBody Map<String, Object> body,
                                      @RequestHeader("X-User-Name") String username) {
    Optional<User> teacher = userRepository.findByUsername(username);
    if (teacher.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "教师不存在"));

    TrainingTask task = new TrainingTask();
    if (body.get("classId") == null || body.get("scenarioId") == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "缺少必填参数 classId 或 scenarioId"));
    }
    task.setClassId(Long.valueOf(body.get("classId").toString()));
    task.setScenarioId(Long.valueOf(body.get("scenarioId").toString()));
    task.setTitle(body.get("title") != null ? body.get("title").toString() : "");
    task.setDescription(body.get("description") != null ? body.get("description").toString() : "");
    task.setTeacherId(teacher.get().getId());
    task.setTeacherName(teacher.get().getFullName());
    task.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

    return ResponseEntity.ok(trainingTaskRepository.save(task));
  }

  /** 删除实训任务 */
  @DeleteMapping("/tasks/{taskId}")
  public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
    if (trainingTaskRepository.existsById(taskId)) {
      trainingTaskRepository.deleteById(taskId);
      return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
    return ResponseEntity.badRequest().body(Map.of("error", "任务不存在"));
  }

  /** 获取实训任务的词云数据（学生对话中使用最多的词语） */
  @GetMapping("/tasks/{taskId}/word-cloud")
  public ResponseEntity<?> getTaskWordCloud(@PathVariable Long taskId) {
    Optional<TrainingTask> taskOpt = trainingTaskRepository.findById(taskId);
    if (taskOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "任务不存在"));

    TrainingTask task = taskOpt.get();
    List<User> classStudents = userRepository.findAll().stream()
        .filter(u -> "student".equalsIgnoreCase(u.getRole()) && task.getClassId().equals(u.getClassId()))
        .collect(Collectors.toList());

    // 收集该班级学生在相关场景中的对话内容
    List<String> allWords = new ArrayList<>();
    for (User student : classStudents) {
      List<BehaviorRecord> behaviors = behaviorRecordRepository.findByUserId(student.getId());
      for (BehaviorRecord b : behaviors) {
        if ("chat".equalsIgnoreCase(b.getActionType()) || "message".equalsIgnoreCase(b.getActionType())) {
          String detail = b.getDetail();
          if (detail != null && !detail.isEmpty()) {
            allWords.add(detail);
          }
        }
      }
    }

    // 对中文文本进行简单分词和词频统计
    Map<String, Integer> wordFreq = getWordFrequency(allWords);

    // 取前50个高频词
    List<Map<String, Object>> wordCloudData = wordFreq.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(50)
        .map(e -> {
          Map<String, Object> item = new HashMap<>();
          item.put("name", e.getKey());
          item.put("value", e.getValue());
          return item;
        }).collect(Collectors.toList());

    return ResponseEntity.ok(Map.of(
        "taskId", taskId,
        "taskTitle", task.getTitle(),
        "wordCloud", wordCloudData,
        "totalWords", allWords.size()
    ));
  }

  /** 获取实训任务中所有学生的完成情况 */
  @GetMapping("/tasks/{taskId}/students")
  public ResponseEntity<?> getTaskStudents(@PathVariable Long taskId) {
    Optional<TrainingTask> taskOpt = trainingTaskRepository.findById(taskId);
    if (taskOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "任务不存在"));

    TrainingTask task = taskOpt.get();
    List<User> classStudents = userRepository.findAll().stream()
        .filter(u -> "student".equalsIgnoreCase(u.getRole()) && task.getClassId().equals(u.getClassId()))
        .collect(Collectors.toList());

    List<Map<String, Object>> result = new ArrayList<>();
    for (User student : classStudents) {
      List<TrainingRecord> records = trainingRecordRepository.findByStudentId(student.getId());
      Optional<TrainingRecord> taskRecord = records.stream()
          .filter(r -> task.getScenarioId().equals(r.getScenarioId()))
          .findFirst();

      // 统计该学生的对话次数
      List<BehaviorRecord> behaviors = behaviorRecordRepository.findByUserId(student.getId());
      long chatCount = behaviors.stream()
          .filter(b -> "chat".equalsIgnoreCase(b.getActionType()) || "message".equalsIgnoreCase(b.getActionType()))
          .count();

      Map<String, Object> info = new HashMap<>();
      info.put("studentId", student.getId());
      info.put("studentName", student.getFullName());
      info.put("username", student.getUsername());
      info.put("grade", student.getGrade());
      info.put("completed", taskRecord.isPresent());
      info.put("score", taskRecord.map(TrainingRecord::getScore).orElse(0));
      info.put("maxScore", taskRecord.map(TrainingRecord::getMaxScore).orElse(100));
      info.put("status", taskRecord.map(TrainingRecord::getStatus).orElse("未开始"));
      info.put("completedAt", taskRecord.map(TrainingRecord::getCompletedAt).orElse(""));
      info.put("chatCount", (int) chatCount);
      result.add(info);
    }

    return ResponseEntity.ok(result);
  }

  /** 获取某个学生在某个实训任务中的所有对话回答 */
  @GetMapping("/tasks/{taskId}/students/{studentId}/responses")
  public ResponseEntity<?> getStudentTaskResponses(@PathVariable Long taskId,
                                                    @PathVariable Long studentId) {
    Optional<TrainingTask> taskOpt = trainingTaskRepository.findById(taskId);
    Optional<User> studentOpt = userRepository.findById(studentId);
    if (taskOpt.isEmpty() || studentOpt.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "任务或学生不存在"));
    }

    TrainingTask task = taskOpt.get();
    User student = studentOpt.get();

    // 获取训练记录
    List<TrainingRecord> records = trainingRecordRepository.findByStudentId(studentId).stream()
        .filter(r -> task.getScenarioId().equals(r.getScenarioId()))
        .collect(Collectors.toList());

    // 获取所有行为记录（对话和回复）
    List<BehaviorRecord> allBehaviors = behaviorRecordRepository.findByUserId(studentId);
    List<BehaviorRecord> chatMessages = allBehaviors.stream()
        .filter(b -> "chat".equalsIgnoreCase(b.getActionType())
            || "message".equalsIgnoreCase(b.getActionType())
            || "reply".equalsIgnoreCase(b.getActionType()))
        .collect(Collectors.toList());

    Map<String, Object> result = new HashMap<>();
    result.put("studentId", studentId);
    result.put("studentName", student.getFullName());
    result.put("taskId", taskId);
    result.put("taskTitle", task.getTitle());
    result.put("scenarioId", task.getScenarioId());
    result.put("trainingRecords", records);
    result.put("messages", chatMessages);
    result.put("totalMessages", chatMessages.size());

    return ResponseEntity.ok(result);
  }

  // ==================== 词频分析辅助方法 ====================

  /** 简单的中文词频统计 */
  private Map<String, Integer> getWordFrequency(List<String> texts) {
    Map<String, Integer> freq = new LinkedHashMap<>();
    // 常见的停用词
    Set<String> stopWords = new HashSet<>(Arrays.asList(
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一",
        "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着",
        "没有", "看", "好", "自己", "这", "他", "她", "它", "们", "那", "些",
        "什么", "怎么", "如何", "为什么", "可以", "这个", "那个", "还", "被",
        "把", "让", "对", "从", "能", "想", "知道", "觉得", "应该", "可能",
        "吗", "呢", "啊", "吧", "哦", "嗯", "呀", "哈", "哇"
    ));

    for (String text : texts) {
      // 按中文分词简单处理：每2-3个字符为一个词
      String clean = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z]", "");
      for (int i = 0; i < clean.length(); i++) {
        // 取2-3字词
        for (int len = 1; len <= 3 && i + len <= clean.length(); len++) {
          String word = clean.substring(i, i + len);
          if (word.length() >= 2 && !stopWords.contains(word) && !word.matches("^[a-zA-Z]+$")) {
            freq.merge(word, 1, Integer::sum);
          }
        }
      }
    }
    return freq;
  }
}
