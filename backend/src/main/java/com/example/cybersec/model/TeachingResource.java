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
@Table(name = "teaching_resource")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeachingResource {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String title;
  private String resourceType;
  private String fileUrl;
  private String description;
  private String tags;
  private Long uploadedBy;
  
  /** 上传者角色：admin 或 teacher */
  private String uploaderRole;
  
  /** 可见性：ALL（所有人可见）、TEACHER_CLASSES（仅该教师所管班级可见）、SPECIFIC_CLASSES（指定班级可见） */
  private String visibility;
  
  /** 可见班级ID列表，逗号分隔（visibility=SPECIFIC_CLASSES 时生效） */
  private String visibleClassIds;
  
  /** 上传教师ID（教师上传时记录） */
  private Long teacherId;
  
  /** 上传日期 */
  private String uploadDate;
}

