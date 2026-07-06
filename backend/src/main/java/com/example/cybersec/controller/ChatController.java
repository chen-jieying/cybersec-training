package com.example.cybersec.controller;

import com.example.cybersec.model.BehaviorRecord;
import com.example.cybersec.model.TrainingRecord;
import com.example.cybersec.model.User;
import com.example.cybersec.repository.BehaviorRecordRepository;
import com.example.cybersec.repository.TrainingRecordRepository;
import com.example.cybersec.repository.UserRepository;
import com.example.cybersec.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  private final ChatService chatService;
  private final BehaviorRecordRepository behaviorRecordRepository;
  private final TrainingRecordRepository trainingRecordRepository;
  private final UserRepository userRepository;

  public ChatController(ChatService chatService,
                        BehaviorRecordRepository behaviorRecordRepository,
                        TrainingRecordRepository trainingRecordRepository,
                        UserRepository userRepository) {
    this.chatService = chatService;
    this.behaviorRecordRepository = behaviorRecordRepository;
    this.trainingRecordRepository = trainingRecordRepository;
    this.userRepository = userRepository;
  }

  /**
   * 简单对话（旧接口兼容）
   */
  @PostMapping
  public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
    String message = body.getOrDefault("message", "");
    String reply = chatService.reply(message, "phishing", 1);
    return ResponseEntity.ok(Map.of("reply", reply));
  }

  /**
   * 情景实训对话接口 - 支持多轮对话、灵活回复、记录保存
   */
  @PostMapping("/scenario")
  public ResponseEntity<?> scenarioChat(@RequestBody Map<String, Object> body,
                                         @RequestHeader("X-User-Name") String username) {
    String message = body.get("message") != null ? body.get("message").toString() : "";
    String sceneType = body.get("sceneType") != null ? body.get("sceneType").toString() : "phishing";
    int turnNumber = body.get("turnNumber") != null ? Integer.parseInt(body.get("turnNumber").toString()) : 1;
    Long scenarioId = body.get("scenarioId") != null ? Long.parseLong(body.get("scenarioId").toString()) : 1L;

    // 用户对话标识（区分不同场景的对话历史）
    String userKey = username + "_scenario_" + scenarioId;

    // 保存学生消息到行为记录
    saveBehavior(username, message, "chat", scenarioId, turnNumber, true);

    // 生成AI回复（优先使用DeepSeek API，带入对话历史）
    String aiReply = chatService.replyWithContext(message, sceneType, turnNumber, userKey);

    // 保存AI回复到行为记录
    saveBehavior(username, aiReply, "reply", scenarioId, turnNumber, false);

    // 分析安全意识
    int awareness = chatService.analyzeSafetyAwareness(message);

    // 判断是否结束：安全意识极高 或 对话轮次达到上限
    boolean isEnd = awareness >= 80 || turnNumber >= 6;

    Map<String, Object> result = new HashMap<>();
    result.put("reply", aiReply);
    result.put("awareness", awareness);
    result.put("turnNumber", turnNumber);
    result.put("isTrainingEnd", isEnd);

    return ResponseEntity.ok(result);
  }

  /**
   * 结束实训 - 保存训练记录（不含AI评分）
   */
  @PostMapping("/end-training")
  public ResponseEntity<?> endTraining(@RequestBody Map<String, Object> body,
                                        @RequestHeader("X-User-Name") String username) {
    Long scenarioId = body.get("scenarioId") != null ? Long.parseLong(body.get("scenarioId").toString()) : null;
    String scenarioTitle = body.get("scenarioTitle") != null ? body.get("scenarioTitle").toString() : "未知场景";
    String sceneType = body.get("sceneType") != null ? body.get("sceneType").toString() : "phishing";
    int turnCount = body.get("turnCount") != null ? Integer.parseInt(body.get("turnCount").toString()) : 0;

    Optional<User> studentOpt = userRepository.findByUsername(username);
    if (studentOpt.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
    }

    User student = studentOpt.get();
    String now = LocalDateTime.now().toString();

    // 清除该场景的对话历史
    chatService.clearHistory(username + "_scenario_" + scenarioId);

    // 保存训练记录（score留空，等待教师评分）
    TrainingRecord record = new TrainingRecord();
    record.setStudentId(student.getId());
    record.setScenarioId(scenarioId != null ? scenarioId : 1L);
    record.setScenarioTitle(scenarioTitle);
    record.setScore(0);
    record.setMaxScore(100);
    record.setStatus("待教师评分");
    record.setCompletedAt(now);
    TrainingRecord saved = trainingRecordRepository.save(record);

    // 保存行为记录
    BehaviorRecord behavior = new BehaviorRecord();
    behavior.setUserId(student.getId());
    behavior.setActionType("training_complete");
    behavior.setDetail("完成情景实训：" + scenarioTitle + "，共" + turnCount + "轮对话，等待教师评分");
    behavior.setCreatedAt(now);
    behaviorRecordRepository.save(behavior);

    Map<String, Object> result = new HashMap<>();
    result.put("id", saved.getId());
    result.put("status", "待教师评分");
    result.put("message", "实训已结束，你的对话记录已提交给教师评阅。");

    return ResponseEntity.ok(result);
  }

  /**
   * 获取学生的情景实训完成状态
   */
  @GetMapping("/training-status")
  public ResponseEntity<?> getTrainingStatus(@RequestHeader("X-User-Name") String username) {
    Optional<User> studentOpt = userRepository.findByUsername(username);
    if (studentOpt.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
    }

    List<TrainingRecord> records = trainingRecordRepository.findByStudentId(studentOpt.get().getId());

    // 按场景分组统计
    Map<Long, Map<String, Object>> statusMap = new LinkedHashMap<>();
    for (TrainingRecord r : records) {
      Long sid = r.getScenarioId();
      if (!statusMap.containsKey(sid)) {
        Map<String, Object> info = new HashMap<>();
        info.put("scenarioId", sid);
        info.put("scenarioTitle", r.getScenarioTitle());
        info.put("completed", true);
        info.put("status", r.getStatus());
        info.put("score", r.getScore());
        info.put("maxScore", r.getMaxScore());
        info.put("completedAt", r.getCompletedAt());
        statusMap.put(sid, info);
      }
    }

    return ResponseEntity.ok(new ArrayList<>(statusMap.values()));
  }

  private void saveBehavior(String username, String content, String actionType,
                            Long scenarioId, int turnNumber, boolean isUser) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) return;

    BehaviorRecord record = new BehaviorRecord();
    record.setUserId(userOpt.get().getId());
    record.setActionType(actionType);
    String detail = (isUser ? "[学生" : "[AI教练") + " 第" + turnNumber + "轮 场景" + scenarioId + "] " +
        (content.length() > 500 ? content.substring(0, 500) + "..." : content);
    record.setDetail(detail);
    record.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    behaviorRecordRepository.save(record);
  }
}
