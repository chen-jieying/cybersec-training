<template>
  <div class="student-exam-page">
    <el-card shadow="hover" v-loading="loading">
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

      <el-empty v-if="!loading && questions.length === 0" description="该关卡暂无题目" />

      <div class="questions-list">
        <el-card v-for="(q, index) in questions" :key="q.id" shadow="hover" class="question-card">
          <div class="question-header">
            <span class="question-num">第{{ index + 1 }}题</span>
            <el-tag size="small">{{ q.category }}</el-tag>
            <span class="question-score">{{ q.score }}分</span>
          </div>
          <p class="question-text">{{ q.questionText }}</p>
          <el-radio-group v-model="answers[q.id]" class="options-group">
            <el-radio v-for="opt in ['A','B','C','D']" :key="opt" :value="opt" class="option-item">
              <span class="option-label">{{ opt }}.</span>
              {{ q['option' + opt] }}
            </el-radio>
          </el-radio-group>
        </el-card>
      </div>

      <div class="submit-area">
        <el-button type="primary" size="large" @click="handleSubmit" :disabled="!canSubmit" :loading="submitting">
          <el-icon><Finished /></el-icon> 提交试卷
        </el-button>
        <span v-if="!canSubmit && questions.length > 0" class="submit-hint">还有 {{ questions.length - answeredCount }} 题未作答</span>
        <span v-if="questions.length === 0 && !loading" class="submit-hint" style="color:#F56C6C">题目数据加载失败，请刷新页面</span>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Finished } from '@element-plus/icons-vue';
import { getStageQuestions } from '../../api';
import api from '../../api';

const route = useRoute();
const router = useRouter();
const stageId = computed(() => Number(route.params.stageId));
const loading = ref(false);
const submitting = ref(false);
const questions = ref<any[]>([]);

const stageNames = ['', '第一关：安全基础', '第二关：钓鱼防护', '第三关：社交工程'];
const stageName = computed(() => stageNames[stageId.value] || `第${stageId.value}关`);

const answers = reactive<Record<number, string>>({});
const totalScore = computed(() => questions.value.reduce((sum, q) => sum + (q.score || 0), 0));
const answeredCount = computed(() => questions.value.filter(q => answers[q.id]).length);
const canSubmit = computed(() => questions.value.length > 0 && answeredCount.value >= questions.value.length);

const loadQuestions = async () => {
  loading.value = true;
  try {
    console.log('[Exam] Loading questions for stage:', stageId.value);
    const res = await getStageQuestions(stageId.value);
    questions.value = res.data || [];
    console.log('[Exam] Loaded questions:', questions.value.length);
    if (questions.value.length === 0) {
      ElMessage.warning('该关卡暂无题目');
    }
  } catch (e: any) {
    console.error('[Exam] Failed to load questions:', e);
    ElMessage.error('加载题目失败，请刷新页面重试');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  console.log('[Exam] handleSubmit called, questions:', questions.value.length, 'answered:', answeredCount.value);

  // 检查是否有题目
  if (questions.value.length === 0) {
    ElMessage.warning('没有可提交的题目');
    return;
  }

  // 检查是否全部作答
  if (answeredCount.value < questions.value.length) {
    ElMessage.warning(`请完成所有题目后再提交（已答${answeredCount.value}/${questions.value.length}）`);
    return;
  }

  // 确认提交
  try {
    await ElMessageBox.confirm('确定要提交试卷吗？提交后将自动判分。', '提交确认', {
      type: 'warning',
      confirmButtonText: '确定提交',
      cancelButtonText: '继续检查'
    });
  } catch {
    console.log('[Exam] User cancelled submission');
    return;
  }

  submitting.value = true;
  console.log('[Exam] Submitting exam...');

  try {
    const answerList = questions.value.map(q => ({
      questionId: q.id,
      selectedOption: answers[q.id] || ''
    }));
    console.log('[Exam] Answer list:', JSON.stringify(answerList));

    const res = await api.post('/student/exam/submit', {
      stageId: stageId.value,
      answers: answerList
    });
    console.log('[Exam] Submit response:', res.data);

    const data = res.data;

    // 存储结果用于结果页展示
    sessionStorage.setItem(`examResult_${stageId.value}`, JSON.stringify({
      score: data.score,
      totalScore: data.totalScore,
      results: data.results,
      stageName: stageName.value,
      examSessionId: data.examSessionId
    }));

    ElMessage.success(`试卷提交成功！得分：${data.score}/${data.totalScore}`);
    router.push(`/student/result/${stageId.value}`);
  } catch (err: any) {
    console.error('[Exam] Submit failed:', err);
    const errorMsg = err?.response?.data?.error || err?.response?.data?.message || err?.message || '提交失败，请重试';
    ElMessage.error(errorMsg);
  } finally {
    submitting.value = false;
  }
};

onMounted(loadQuestions);
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
