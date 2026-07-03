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
}

