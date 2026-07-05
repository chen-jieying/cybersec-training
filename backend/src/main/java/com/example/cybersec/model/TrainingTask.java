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
 * 实训任务 - 教师发布给班级的实训任务
 */
@Entity
@Table(name = "training_task")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  /** 所属班级ID */
  private Long classId;
  
  /** 关联的情景剧本ID */
  private Long scenarioId;
  
  /** 任务标题 */
  private String title;
  
  /** 任务描述 */
  private String description;
  
  /** 发布教师ID */
  private Long teacherId;
  
  /** 教师姓名 */
  private String teacherName;
  
  /** 创建时间 */
  private String createdAt;
}
