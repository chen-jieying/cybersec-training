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
@Table(name = "training_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private Long studentId;
  private Long scenarioId;
  private String scenarioTitle;
  private Integer score;
  private Integer maxScore;
  private String status;
  private String completedAt;
}

