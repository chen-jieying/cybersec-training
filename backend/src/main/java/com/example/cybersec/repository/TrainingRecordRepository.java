package com.example.cybersec.repository;

import com.example.cybersec.model.TrainingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {
  List<TrainingRecord> findByStudentId(Long studentId);

  /** 按学生ID和场景ID查询训练记录 */
  List<TrainingRecord> findByStudentIdAndScenarioId(Long studentId, Long scenarioId);
}

