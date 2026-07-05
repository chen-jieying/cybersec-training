package com.example.cybersec.repository;

import com.example.cybersec.model.TrainingTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingTaskRepository extends JpaRepository<TrainingTask, Long> {
  /** 按班级ID查询实训任务 */
  List<TrainingTask> findByClassId(Long classId);

  /** 按教师ID查询实训任务 */
  List<TrainingTask> findByTeacherId(Long teacherId);
}
