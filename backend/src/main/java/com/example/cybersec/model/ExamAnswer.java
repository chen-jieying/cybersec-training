package com.example.cybersec.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生考试答题记录 - 存储每道题的作答详情
 * 每次答题每道题都会生成一条记录，用于追溯学生选择
 */
@Entity
@Table(name = "exam_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 学生ID */
  private Long studentId;

  /** 题目ID */
  private Long questionId;

  /** 学生选择的选项 (A/B/C/D) */
  private String selectedOption;

  /** 正确答案 */
  private String correctAnswer;

  /** 是否答对 */
  private Boolean isCorrect;

  /** 本题得分 */
  private Integer score;

  /** 所属关卡ID */
  private Long stageId;

  /** 考试会话ID - 同一次答题的所有记录共享同一个sessionId */
  private String examSessionId;

  /** 答题时间 */
  private String answeredAt;

  /** 所属分类 */
  private String category;
}
