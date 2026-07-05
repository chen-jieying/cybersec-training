package com.example.cybersec.controller;

import com.example.cybersec.model.Question;
import com.example.cybersec.model.SchoolClass;
import com.example.cybersec.model.ScenarioScript;
import com.example.cybersec.model.TeachingResource;
import com.example.cybersec.model.User;
import com.example.cybersec.repository.QuestionRepository;
import com.example.cybersec.repository.SchoolClassRepository;
import com.example.cybersec.repository.ScenarioScriptRepository;
import com.example.cybersec.repository.TeachingResourceRepository;
import com.example.cybersec.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final UserRepository userRepository;
  private final SchoolClassRepository schoolClassRepository;
  private final QuestionRepository questionRepository;
  private final ScenarioScriptRepository scenarioScriptRepository;
  private final TeachingResourceRepository teachingResourceRepository;

  public AdminController(UserRepository userRepository,
                         SchoolClassRepository schoolClassRepository,
                         QuestionRepository questionRepository,
                         ScenarioScriptRepository scenarioScriptRepository,
                         TeachingResourceRepository teachingResourceRepository) {
    this.userRepository = userRepository;
    this.schoolClassRepository = schoolClassRepository;
    this.questionRepository = questionRepository;
    this.scenarioScriptRepository = scenarioScriptRepository;
    this.teachingResourceRepository = teachingResourceRepository;
  }

  // ==================== User Management ====================
  @GetMapping("/users")
  public List<User> listUsers() {
    return userRepository.findAll();
  }

  @PostMapping("/users")
  public User createUser(@RequestBody User user) {
    return userRepository.save(user);
  }

  @PutMapping("/users/{id}")
  public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
    Optional<User> existing = userRepository.findById(id);
    if (existing.isEmpty()) return ResponseEntity.notFound().build();
    user.setId(id);
    return ResponseEntity.ok(userRepository.save(user));
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
    userRepository.deleteById(id);
    return ResponseEntity.ok().build();
  }

  // ==================== Class Management ====================
  @GetMapping("/classes")
  public List<SchoolClass> listClasses() {
    return schoolClassRepository.findAll();
  }

  @PostMapping("/classes")
  public SchoolClass createClass(@RequestBody SchoolClass schoolClass) {
    return schoolClassRepository.save(schoolClass);
  }

  @PutMapping("/classes/{id}")
  public ResponseEntity<?> updateClass(@PathVariable Long id, @RequestBody SchoolClass schoolClass) {
    Optional<SchoolClass> existing = schoolClassRepository.findById(id);
    if (existing.isEmpty()) return ResponseEntity.notFound().build();
    schoolClass.setId(id);
    return ResponseEntity.ok(schoolClassRepository.save(schoolClass));
  }

  @DeleteMapping("/classes/{id}")
  public ResponseEntity<?> deleteClass(@PathVariable Long id) {
    if (!schoolClassRepository.existsById(id)) return ResponseEntity.notFound().build();
    schoolClassRepository.deleteById(id);
    return ResponseEntity.ok().build();
  }

  // ==================== Question Management ====================
  @GetMapping("/questions")
  public List<Question> listQuestions() {
    return questionRepository.findAll();
  }

  @PostMapping("/questions")
  public Question createQuestion(@RequestBody Question question) {
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

  @DeleteMapping("/questions/{id}")
  public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
    if (!questionRepository.existsById(id)) return ResponseEntity.notFound().build();
    questionRepository.deleteById(id);
    return ResponseEntity.ok().build();
  }

  // ==================== Scenario Management ====================
  @GetMapping("/scenarios")
  public List<ScenarioScript> listScenarios() {
    return scenarioScriptRepository.findAll();
  }

  @PostMapping("/scenarios")
  public ScenarioScript createScenario(@RequestBody ScenarioScript scenarioScript) {
    return scenarioScriptRepository.save(scenarioScript);
  }

  @PutMapping("/scenarios/{id}")
  public ResponseEntity<?> updateScenario(@PathVariable Long id, @RequestBody ScenarioScript scenarioScript) {
    Optional<ScenarioScript> existing = scenarioScriptRepository.findById(id);
    if (existing.isEmpty()) return ResponseEntity.notFound().build();
    scenarioScript.setId(id);
    return ResponseEntity.ok(scenarioScriptRepository.save(scenarioScript));
  }

  @DeleteMapping("/scenarios/{id}")
  public ResponseEntity<?> deleteScenario(@PathVariable Long id) {
    if (!scenarioScriptRepository.existsById(id)) return ResponseEntity.notFound().build();
    scenarioScriptRepository.deleteById(id);
    return ResponseEntity.ok().build();
  }

  // ==================== Resource Management ====================
  @GetMapping("/resources")
  public List<TeachingResource> listResources() {
    return teachingResourceRepository.findAll();
  }

  @PostMapping("/resources")
  public TeachingResource createResource(@RequestBody TeachingResource resource) {
    // 管理员资源默认全局可见
    if (resource.getUploaderRole() == null) resource.setUploaderRole("ADMIN");
    if (resource.getVisibility() == null) resource.setVisibility("ALL");
    if (resource.getUploadDate() == null)
      resource.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    return teachingResourceRepository.save(resource);
  }

  @PutMapping("/resources/{id}")
  public ResponseEntity<?> updateResource(@PathVariable Long id, @RequestBody TeachingResource resource) {
    Optional<TeachingResource> existing = teachingResourceRepository.findById(id);
    if (existing.isEmpty()) return ResponseEntity.notFound().build();
    resource.setId(id);
    return ResponseEntity.ok(teachingResourceRepository.save(resource));
  }

  @DeleteMapping("/resources/{id}")
  public ResponseEntity<?> deleteResource(@PathVariable Long id) {
    if (!teachingResourceRepository.existsById(id)) return ResponseEntity.notFound().build();
    teachingResourceRepository.deleteById(id);
    return ResponseEntity.ok().build();
  }
}

