package com.example.cybersec.config;

import com.example.cybersec.model.ScenarioScript;
import com.example.cybersec.model.SchoolClass;
import com.example.cybersec.model.TeachingResource;
import com.example.cybersec.model.User;
import com.example.cybersec.repository.ScenarioScriptRepository;
import com.example.cybersec.repository.SchoolClassRepository;
import com.example.cybersec.repository.TeachingResourceRepository;
import com.example.cybersec.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

  @Bean
  public CommandLineRunner initData(
      UserRepository userRepository,
      SchoolClassRepository schoolClassRepository,
      ScenarioScriptRepository scenarioScriptRepository,
      TeachingResourceRepository teachingResourceRepository) {
    return args -> {
      if (userRepository.findByUsername("student1").isEmpty()) {
        userRepository.save(new User(null, "student1", "123456", "student", "Zhang San", "Junior Two", 1L, "Student", "13800000001"));
      }
      if (userRepository.findByUsername("teacher1").isEmpty()) {
        userRepository.save(new User(null, "teacher1", "123456", "teacher", "Teacher Li", "None", null, "Teacher", "13800000002"));
      }
      if (userRepository.findByUsername("admin1").isEmpty()) {
        userRepository.save(new User(null, "admin1", "123456", "admin", "Administrator", "None", null, "Admin", "13800000003"));
      }

      if (schoolClassRepository.findAll().isEmpty()) {
        schoolClassRepository.save(new SchoolClass(null, "Junior Two", "Class 2", 2L, "Teacher Li"));
      }

      if (scenarioScriptRepository.findAll().isEmpty()) {
        scenarioScriptRepository.save(new ScenarioScript(null, "Phishing", "Campus phishing drill", "Simulate a phishing scenario where a fake homeroom teacher requests a transfer.", "Built-in"));
        scenarioScriptRepository.save(new ScenarioScript(null, "Account Safety", "Account leak drill", "Simulate a security education scenario where a stranger lures the user to reveal credentials.", "Built-in"));
      }

      if (teachingResourceRepository.findAll().isEmpty()) {
        teachingResourceRepository.save(new TeachingResource(null, "Cybersecurity handbook", "pdf", "/files/security-manual.pdf", "Network security learning material for junior high students.", "Security, Basics", 2L));
      }
    };
  }
}
