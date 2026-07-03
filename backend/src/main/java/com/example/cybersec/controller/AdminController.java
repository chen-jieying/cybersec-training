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

import java.util.List;

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

  @GetMapping("/users")
  public List<User> listUsers() {
    return userRepository.findAll();
  }

  @PostMapping("/users")
  public User createUser(@RequestBody User user) {
    return userRepository.save(user);
  }

  @GetMapping("/classes")
  public List<SchoolClass> listClasses() {
    return schoolClassRepository.findAll();
  }

  @PostMapping("/classes")
  public SchoolClass createClass(@RequestBody SchoolClass schoolClass) {
    return schoolClassRepository.save(schoolClass);
  }

  @GetMapping("/questions")
  public List<Question> listQuestions() {
    return questionRepository.findAll();
  }

  @PostMapping("/questions")
  public Question createQuestion(@RequestBody Question question) {
    return questionRepository.save(question);
  }

  @GetMapping("/scenarios")
  public List<ScenarioScript> listScenarios() {
    return scenarioScriptRepository.findAll();
  }

  @PostMapping("/scenarios")
  public ScenarioScript createScenario(@RequestBody ScenarioScript scenarioScript) {
    return scenarioScriptRepository.save(scenarioScript);
  }

  @GetMapping("/resources")
  public List<TeachingResource> listResources() {
    return teachingResourceRepository.findAll();
  }

  @PostMapping("/resources")
  public TeachingResource createResource(@RequestBody TeachingResource resource) {
    return teachingResourceRepository.save(resource);
  }
}

