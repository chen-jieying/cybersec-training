package com.example.cybersec.repository;

import com.example.cybersec.model.ScenarioDialogue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenarioDialogueRepository extends JpaRepository<ScenarioDialogue, Long> {
  List<ScenarioDialogue> findByScriptIdOrderByStepIndex(Long scriptId);
}

