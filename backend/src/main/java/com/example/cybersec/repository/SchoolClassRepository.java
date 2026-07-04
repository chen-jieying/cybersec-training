package com.example.cybersec.repository;

import com.example.cybersec.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
  /** 按教师ID查询该教师负责的班级 */
  List<SchoolClass> findByTeacherId(Long teacherId);
}

