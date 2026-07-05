package com.example.cybersec.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scenario_dialogue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioDialogue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long scriptId;
  private Integer stepIndex;
  private String botText;
  private String optionA;
  private String optionB;
  private String optionC;
  private String optionD;
  private String scoreA;
  private String scoreB;
  private String scoreC;
  private String scoreD;
}

