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
@Table(name = "school_class")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClass {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String grade;
  private String className;
  private Long teacherId;
  private String teacherName;
}

