package com.example.cybersec.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "question_bank")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String category;
  @Column(name = "question_text")
  private String questionText;
  @Column(name = "option_a")
  private String optionA;
  @Column(name = "option_b")
  private String optionB;
  @Column(name = "option_c")
  private String optionC;
  @Column(name = "option_d")
  private String optionD;
  private String answer;
  private String type;
  private Integer score;
  /** 题目解析 - 展示正确答案的详细解释 */
  @Column(length = 2000)
  private String explanation;
}

