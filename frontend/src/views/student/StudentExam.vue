<template>
  <div class="student-exam-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>{{ stageName }}</h3>
          <p>请认真作答每一道题，完成所有题目后提交试卷</p>
        </div>
        <div class="exam-info">
          <el-tag type="primary">共 {{ questions.length }} 题</el-tag>
          <el-tag type="success" style="margin-left:8px">总分 {{ totalScore }} 分</el-tag>
          <el-tag style="margin-left:8px">已答 {{ answeredCount }} / {{ questions.length }}</el-tag>
        </div>
      </div>

      <div class="questions-list">
        <el-card
          v-for="(q, index) in questions"
          :key="q.id"
          shadow="hover"
          class="question-card"
        >
          <div class="question-header">
            <span class="question-num">第{{ index + 1 }}题</span>
            <el-tag size="small">{{ q.category }}</el-tag>
            <span class="question-score">{{ q.score }}分</span>
          </div>
          <p class="question-text">{{ q.questionText }}</p>
          <el-radio-group v-model="answers[q.id]" class="options-group">
            <el-radio v-for="opt in ['A','B','C','D']" :key="opt" :value="opt" class="option-item">
              <span class="option-label">{{ opt }}.</span>
              {{ q[`option${opt}` as keyof typeof q] }}
            </el-radio>
          </el-radio-group>
        </el-card>
      </div>

      <div class="submit-area">
        <el-button
          type="primary"
          size="large"
          @click="submitExam"
          :disabled="answeredCount < questions.length"
        >
          <el-icon><Finished /></el-icon> 提交试卷
        </el-button>
        <span v-if="answeredCount < questions.length" class="submit-hint">
          还有 {{ questions.length - answeredCount }} 题未作答
        </span>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Finished } from '@element-plus/icons-vue';
import { mockStages } from '../../mock/data';

const route = useRoute();
const router = useRouter();
const stageId = computed(() => Number(route.params.stageId));
const stage = computed(() => mockStages.find(s => s.id === stageId.value));
const stageName = computed(() => stage.value?.name || '未知关卡');
const questions = computed(() => stage.value?.questions || []);

const answers = ref<Record<number, string>>({});
const totalScore = computed(() => questions.value.reduce((sum, q) => sum + q.score, 0));

const answeredCount = computed(() => {
  return questions.value.filter(q => answers.value[q.id]).length;
});

const submitExam = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要提交试卷吗？提交后将自动判分。',
      '提交确认',
      { type: 'warning', confirmButtonText: '确定提交', cancelButtonText: '继续检查' }
    );

    // Auto scoring
    let score = 0;
    const results: any[] = [];
    questions.value.forEach(q => {
      const userAnswer = answers.value[q.id];
      const correct = userAnswer === q.answer;
      if (correct) score += q.score;
      results.push({
        id: q.id,
        questionText: q.questionText,
        userAnswer,
        correctAnswer: q.answer,
        correct,
        score: correct ? q.score : 0,
      });
    });

    // Store results for result page
    sessionStorage.setItem(`examResult_${stageId.value}`, JSON.stringify({
      score,
      totalScore: totalScore.value,
      results,
      stageName: stageName.value,
    }));

    ElMessage.success(`试卷提交成功！得分：${score}/${totalScore.value}`);
    router.push(`/student/result/${stageId.value}`);
  } catch { /* cancelled */ }
};

onMounted(() => {
  if (!stage.value) {
    ElMessage.error('关卡不存在');
    router.back();
  }
});
</script>

<style scoped>
.student-exam-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.exam-info { display: flex; align-items: center; }
.questions-list { display: flex; flex-direction: column; gap: 16px; }
.question-card { padding: 8px; }
.question-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.question-num { font-weight: 600; color: #409EFF; }
.question-score { margin-left: auto; color: #909399; font-size: 13px; }
.question-text { font-size: 15px; line-height: 1.6; margin-bottom: 16px; color: #303133; }
.options-group { display: flex; flex-direction: column; gap: 10px; width: 100%; }
.option-item { margin-right: 0; padding: 10px 16px; border: 1px solid #ebeef5; border-radius: 8px; width: 100%; transition: border-color 0.2s; }
.option-item:hover { border-color: #409EFF; }
.option-label { font-weight: 600; margin-right: 8px; color: #409EFF; }
.submit-area { text-align: center; margin-top: 24px; }
.submit-hint { display: block; margin-top: 8px; color: #E6A23C; font-size: 13px; }
</style>
