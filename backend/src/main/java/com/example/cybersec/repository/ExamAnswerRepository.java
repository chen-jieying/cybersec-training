package com.example.cybersec.repository;

import com.example.cybersec.model.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {

  /** 查询某学生的所有答题记录 */
  List<ExamAnswer> findByStudentId(Long studentId);

  /** 查询某学生某次考试的所有答题记录 */
  List<ExamAnswer> findByStudentIdAndExamSessionId(Long studentId, String examSessionId);

  /** 查询某学生某关卡的答题记录 */
  List<ExamAnswer> findByStudentIdAndStageId(Long studentId, Long stageId);

  /** 查询某次考试会话的所有记录 */
  List<ExamAnswer> findByExamSessionId(String examSessionId);

  /** 统计某学生某关卡的答题次数 */
  long countByStudentIdAndStageId(Long studentId, Long stageId);
}
