package com.example.cybersec.controller;

import com.example.cybersec.model.*;
import com.example.cybersec.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生端控制器 - 支持闯关答题、提交答卷、查看结果、个人中心
 * 所有操作都与数据库交互，不使用虚拟数据
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

  private final UserRepository userRepository;
  private final ScenarioScriptRepository scenarioScriptRepository;
  private final TrainingRecordRepository trainingRecordRepository;
  private final QuestionRepository questionRepository;
  private final ExamAnswerRepository examAnswerRepository;
  private final BehaviorRecordRepository behaviorRecordRepository;

  public StudentController(UserRepository userRepository,
                           ScenarioScriptRepository scenarioScriptRepository,
                           TrainingRecordRepository trainingRecordRepository,
                           QuestionRepository questionRepository,
                           ExamAnswerRepository examAnswerRepository,
                           BehaviorRecordRepository behaviorRecordRepository) {
    this.userRepository = userRepository;
    this.scenarioScriptRepository = scenarioScriptRepository;
    this.trainingRecordRepository = trainingRecordRepository;
    this.questionRepository = questionRepository;
    this.examAnswerRepository = examAnswerRepository;
    this.behaviorRecordRepository = behaviorRecordRepository;
  }

  // ==================== 个人中心 ====================

  /** 获取学生个人资料 */
  @GetMapping("/profile")
  public ResponseEntity<?> getProfile(@RequestHeader("X-User-Name") String username) {
    Optional<User> student = userRepository.findByUsername(username);
    if (student.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
    }

    User user = student.get();
    List<ExamAnswer> allAnswers = examAnswerRepository.findByStudentId(user.getId());

    Map<String, Object> profile = new HashMap<>();
    profile.put("user", user);

    // 统计数据（基于真实答题记录）
    long totalQuestions = allAnswers.size();
    long correctQuestions = allAnswers.stream().filter(ExamAnswer::getIsCorrect).count();
    profile.put("totalQuestions", totalQuestions);
    profile.put("correctQuestions", correctQuestions);
    profile.put("wrongQuestions", totalQuestions - correctQuestions);
    profile.put("accuracy", totalQuestions > 0 ? Math.round((double) correctQuestions / totalQuestions * 100) : 0);

    // 总分
    int totalScore = allAnswers.stream().mapToInt(a -> a.getScore() != null ? a.getScore() : 0).sum();
    profile.put("totalScore", totalScore);

    // 错题列表
    List<Map<String, Object>> wrongList = new ArrayList<>();
    Set<Long> wrongQuestionIds = allAnswers.stream()
        .filter(a -> !Boolean.TRUE.equals(a.getIsCorrect()))
        .map(ExamAnswer::getQuestionId)
        .collect(Collectors.toSet());

    for (Long qid : wrongQuestionIds) {
      questionRepository.findById(qid).ifPresent(q -> {
        ExamAnswer latest = allAnswers.stream()
            .filter(a -> qid.equals(a.getQuestionId()))
            .reduce((first, second) -> second)
            .orElse(null);
        Map<String, Object> wq = new HashMap<>();
        wq.put("id", q.getId());
        wq.put("questionText", q.getQuestionText());
        wq.put("correctAnswer", q.getAnswer());
        wq.put("category", q.getCategory());
        wq.put("explanation", q.getExplanation());
        if (latest != null) {
          wq.put("userAnswer", latest.getSelectedOption());
          wq.put("date", latest.getAnsweredAt());
        }
        wrongList.add(wq);
      });
    }
    profile.put("wrongQuestions", wrongList);

    return ResponseEntity.ok(profile);
  }

  // ==================== 闯关题库 ====================

  /** 获取所有闯关关卡及题目 */
  @GetMapping("/stages")
  public ResponseEntity<?> getStages(@RequestHeader("X-User-Name") String username) {
    Optional<User> student = userRepository.findByUsername(username);
    if (student.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));

    Long studentId = student.get().getId();
    List<Question> allQuestions = questionRepository.findAll();

    // 按 stageId 分组题目（使用现有category字段区分关卡）
    // 同时从 question_score 虚拟表来搭建 stageId
    // 实际按 category 映射到关卡: 基础知识=stage1, 钓鱼防护=stage2, 社交工程=stage3
    Map<Long, List<Question>> stageMap = new LinkedHashMap<>();
    stageMap.put(1L, allQuestions.stream().filter(q -> "基础知识".equals(q.getCategory())).collect(Collectors.toList()));
    stageMap.put(2L, allQuestions.stream().filter(q -> "钓鱼防护".equals(q.getCategory())).collect(Collectors.toList()));
    stageMap.put(3L, allQuestions.stream().filter(q -> "社交工程".equals(q.getCategory())).collect(Collectors.toList()));

    List<Map<String, Object>> stages = new ArrayList<>();
    String[] stageNames = {"第一关：安全基础", "第二关：钓鱼防护", "第三关：社交工程"};
    String[] stageDescs = {
        "掌握网络安全的基本概念和常识",
        "学习识别和防范网络钓鱼攻击",
        "了解社交工程攻击手法和防范措施"
    };

    int idx = 0;
    for (Map.Entry<Long, List<Question>> entry : stageMap.entrySet()) {
      Long stageId = entry.getKey();
      List<Question> qs = entry.getValue();
      if (qs.isEmpty()) continue;

      // 检查是否已解锁（成绩≥60%才解锁）
      boolean unlocked = stageId == 1L; // 第一关默认解锁
      if (stageId > 1L) {
        // 计算前一关的最高得分率
        List<ExamAnswer> prevAnswers = examAnswerRepository.findByStudentIdAndStageId(studentId, stageId - 1);
        int prevTotalScore = prevAnswers.stream()
            .filter(a -> a.getScore() != null)
            .mapToInt(a -> {
              // 根据每题得分推算该题满分
              return a.getScore();
            }).sum();
        // 获取前一关的题目总分
        List<Question> prevQuestions = questionRepository.findAll().stream()
            .filter(q -> getCategoryByStage(stageId - 1).equals(q.getCategory()))
            .collect(Collectors.toList());
        int prevFullScore = prevQuestions.stream().mapToInt(q -> q.getScore() != null ? q.getScore() : 0).sum();
        double passRate = prevFullScore > 0 ? (double) prevTotalScore / prevFullScore : 0;
        unlocked = passRate >= 0.6;
      }

      // 计算该关卡最佳得分
      List<ExamAnswer> stageAnswers = examAnswerRepository.findByStudentIdAndStageId(studentId, stageId);
      double bestScore = 0;
      if (!stageAnswers.isEmpty()) {
        int maxSessionScore = stageAnswers.stream()
            .filter(a -> a.getScore() != null)
            .mapToInt(ExamAnswer::getScore)
            .sum();
        bestScore = maxSessionScore;
      }
      int totalScore = qs.stream().mapToInt(q -> q.getScore() != null ? q.getScore() : 0).sum();
      int stars = totalScore > 0 ? (int) Math.min(3, Math.ceil(bestScore * 3.0 / totalScore)) : 0;

      Map<String, Object> stage = new HashMap<>();
      stage.put("id", stageId);
      stage.put("name", stageNames[idx]);
      stage.put("description", stageDescs[idx]);
      stage.put("unlocked", unlocked);
      stage.put("stars", stars);
      stage.put("questionCount", qs.size());
      stage.put("totalScore", totalScore);
      stage.put("bestScore", (int) bestScore);
      stages.add(stage);
      idx++;
    }

    return ResponseEntity.ok(stages);
  }

  /** 获取某关卡的题目列表（不含答案） */
  @GetMapping("/questions/{stageId}")
  public ResponseEntity<?> getQuestions(@PathVariable Long stageId) {
    // 按 category 映射关卡
    String category;
    switch (stageId.intValue()) {
      case 1: category = "基础知识"; break;
      case 2: category = "钓鱼防护"; break;
      case 3: category = "社交工程"; break;
      default: return ResponseEntity.badRequest().body(Map.of("error", "关卡不存在"));
    }

    List<Question> questions = questionRepository.findAll().stream()
        .filter(q -> category.equals(q.getCategory()))
        .collect(Collectors.toList());

    if (questions.isEmpty()) {
      return ResponseEntity.ok(Collections.emptyList());
    }

    // 返回题目但隐藏正确答案
    List<Map<String, Object>> result = questions.stream().map(q -> {
      Map<String, Object> m = new HashMap<>();
      m.put("id", q.getId());
      m.put("category", q.getCategory());
      m.put("questionText", q.getQuestionText());
      m.put("optionA", q.getOptionA());
      m.put("optionB", q.getOptionB());
      m.put("optionC", q.getOptionC());
      m.put("optionD", q.getOptionD());
      m.put("score", q.getScore());
      m.put("stageId", stageId);
      return m;
    }).collect(Collectors.toList());

    return ResponseEntity.ok(result);
  }

  // ==================== 提交答卷 ====================

  /** 提交考试答卷 - 保存每道题的作答记录到数据库 */
  @PostMapping("/exam/submit")
  public ResponseEntity<?> submitExam(@RequestBody Map<String, Object> payload,
                                       @RequestHeader("X-User-Name") String username) {
    Optional<User> student = userRepository.findByUsername(username);
    if (student.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
    }

    Long studentId = student.get().getId();
    Long stageId = Long.valueOf(payload.get("stageId").toString());

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> answers = (List<Map<String, Object>>) payload.get("answers");

    if (answers == null || answers.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "答题数据为空"));
    }

    String examSessionId = studentId + "_" + stageId + "_" + System.currentTimeMillis();
    String now = LocalDateTime.now().toString();
    int totalScore = 0;
    int earnedScore = 0;
    int correctCount = 0;

    List<Map<String, Object>> results = new ArrayList<>();

    for (Map<String, Object> answer : answers) {
      Long questionId = Long.valueOf(answer.get("questionId").toString());
      String selectedOption = answer.get("selectedOption") != null ? answer.get("selectedOption").toString() : "";

      Optional<Question> qOpt = questionRepository.findById(questionId);
      if (qOpt.isEmpty()) continue;

      Question q = qOpt.get();
      boolean isCorrect = q.getAnswer().equals(selectedOption);
      int itemScore = isCorrect ? (q.getScore() != null ? q.getScore() : 0) : 0;

      if (isCorrect) correctCount++;
      totalScore += (q.getScore() != null ? q.getScore() : 0);
      earnedScore += itemScore;

      // 保存答题记录到数据库
      ExamAnswer record = new ExamAnswer();
      record.setStudentId(studentId);
      record.setQuestionId(questionId);
      record.setSelectedOption(selectedOption);
      record.setCorrectAnswer(q.getAnswer());
      record.setIsCorrect(isCorrect);
      record.setScore(itemScore);
      record.setStageId(stageId);
      record.setExamSessionId(examSessionId);
      record.setAnsweredAt(now);
      record.setCategory(q.getCategory());
      examAnswerRepository.save(record);

      // 构建返回结果（含解析）
      Map<String, Object> resultItem = new HashMap<>();
      resultItem.put("id", q.getId());
      resultItem.put("questionText", q.getQuestionText());
      resultItem.put("userAnswer", selectedOption);
      resultItem.put("correctAnswer", q.getAnswer());
      resultItem.put("correct", isCorrect);
      resultItem.put("score", itemScore);
      resultItem.put("explanation", q.getExplanation() != null ? q.getExplanation() : "");
      results.add(resultItem);
    }

    // 保存训练记录
    TrainingRecord record = new TrainingRecord();
    record.setStudentId(studentId);
    record.setScenarioId(stageId);
    record.setScenarioTitle("第" + stageId + "关");
    record.setScore(earnedScore);
    record.setMaxScore(totalScore);
    record.setStatus("completed");
    record.setCompletedAt(now);
    trainingRecordRepository.save(record);

    // 保存行为记录
    BehaviorRecord behavior = new BehaviorRecord();
    behavior.setUserId(studentId);
    behavior.setActionType("exam");
    behavior.setDetail("完成第" + stageId + "关答题，得分" + earnedScore + "/" + totalScore);
    behavior.setCreatedAt(now);
    behaviorRecordRepository.save(behavior);

    Map<String, Object> result = new HashMap<>();
    result.put("score", earnedScore);
    result.put("totalScore", totalScore);
    result.put("correctCount", correctCount);
    result.put("totalCount", answers.size());
    result.put("results", results);
    result.put("stageId", stageId);
    result.put("examSessionId", examSessionId);

    return ResponseEntity.ok(result);
  }

  /** 获取某次考试的结果详情 */
  @GetMapping("/exam/result/{examSessionId}")
  public ResponseEntity<?> getExamResult(@PathVariable String examSessionId) {
    List<ExamAnswer> answers = examAnswerRepository.findByExamSessionId(examSessionId);
    if (answers.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "未找到答题记录"));
    }

    int totalScore = 0;
    int earnedScore = 0;
    List<Map<String, Object>> results = new ArrayList<>();

    for (ExamAnswer a : answers) {
      questionRepository.findById(a.getQuestionId()).ifPresent(q -> {
        Map<String, Object> item = new HashMap<>();
        item.put("id", q.getId());
        item.put("questionText", q.getQuestionText());
        item.put("userAnswer", a.getSelectedOption());
        item.put("correctAnswer", a.getCorrectAnswer());
        item.put("correct", a.getIsCorrect());
        item.put("score", a.getScore());
        item.put("explanation", q.getExplanation() != null ? q.getExplanation() : "");
        results.add(item);
      });
      totalScore += a.getScore() != null && a.getIsCorrect() ? a.getScore() : 0;
      earnedScore += a.getScore() != null ? a.getScore() : 0;
    }

    Map<String, Object> result = new HashMap<>();
    result.put("score", earnedScore);
    result.put("totalScore", totalScore > 0 ? totalScore : earnedScore);
    result.put("results", results);
    return ResponseEntity.ok(result);
  }

  // ==================== 对话实训 ====================

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

  // ==================== 辅助方法 ====================

  private String getCategoryByStage(Long stageId) {
    switch (stageId.intValue()) {
      case 1: return "基础知识";
      case 2: return "钓鱼防护";
      case 3: return "社交工程";
      default: return "";
    }
  }
}
