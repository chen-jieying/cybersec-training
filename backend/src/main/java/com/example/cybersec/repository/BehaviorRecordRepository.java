package com.example.cybersec.repository;

import com.example.cybersec.model.BehaviorRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BehaviorRecordRepository extends JpaRepository<BehaviorRecord, Long> {
  List<BehaviorRecord> findByUserId(Long userId);
}

