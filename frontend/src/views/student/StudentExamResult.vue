<template>
  <div class="student-result-page">
    <el-card shadow="hover" v-loading="loading">
      <div class="result-header">
        <el-icon :size="64" :color="passColor"><CircleCheckFilled v-if="passed" /><CircleCloseFilled v-else /></el-icon>
        <h2>{{ passed ? '恭喜通关！' : '继续努力！' }}</h2>
        <p>{{ stageName }} 答题结果</p>
      </div>

      <div class="score-display">
        <el-progress type="dashboard" :percentage="percentage" :color="passColor" :stroke-width="12" :width="180">
          <template #default>
            <span class="score-text">{{ score }}</span>
            <span class="score-total">/ {{ totalScore }}</span>
          </template>
        </el-progress>
      </div>

      <div class="result-stats">
        <el-row :gutter="16">
          <el-col :span="8"><el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#67C23A">{{ correctCount }}</div><div class="stat-label">正确题数</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#F56C6C">{{ wrongCount }}</div><div class="stat-label">错误题数</div></el-card></el-col>
          <el-col :span="8"><el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#409EFF">{{ accuracy }}%</div><div class="stat-label">正确率</div></el-card></el-col>
        </el-row>
      </div>

      <div class="detail-section">
        <h4>答题详情</h4>
        <el-collapse v-if="results.length > 0">
          <el-collapse-item v-for="(r, index) in results" :key="r.id"
            :title="`第${index + 1}题：${(r.questionText || '').substring(0, 40)}...`">
            <div class="detail-item" :class="{ correct: r.correct, wrong: !r.correct }">
              <p><strong>题目：</strong>{{ r.questionText }}</p>
              <p><strong>你的答案：</strong><el-tag :type="r.correct ? 'success' : 'danger'">{{ r.userAnswer || '未作答' }}</el-tag></p>
              <p v-if="!r.correct"><strong>正确答案：</strong><el-tag type="success">{{ r.correctAnswer }}</el-tag></p>
              <p><strong>得分：</strong>{{ r.score }} 分</p>
            </div>
            <!-- 题目解析 -->
            <div v-if="r.explanation" class="explanation-box">
              <div class="explanation-header"><el-icon color="#409EFF" :size="18"><InfoFilled /></el-icon><strong>题目解析</strong></div>
              <div class="explanation-content">{{ r.explanation }}</div>
            </div>
          </el-collapse-item>
        </el-collapse>
        <el-empty v-else description="暂无答题详情" />
      </div>

      <div class="result-actions">
        <el-button @click="goBack">返回关卡列表</el-button>
        <el-button v-if="passed" type="primary" @click="goNext">进入下一关</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { CircleCheckFilled, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { getStudentStages } from '../../api';

const route = useRoute();
const router = useRouter();
const stageId = computed(() => Number(route.params.stageId));
const loading = ref(false);

const stageName = ref('');
const score = ref(0);
const totalScore = ref(0);
const results = ref<any[]>([]);

const correctCount = computed(() => results.value.filter(r => r.correct).length);
const wrongCount = computed(() => results.value.filter(r => !r.correct).length);
const accuracy = computed(() => totalScore.value > 0 ? Math.round((score.value / totalScore.value) * 100) : 0);
const passed = computed(() => accuracy.value >= 60);
const percentage = computed(() => accuracy.value);
const passColor = computed(() => passed.value ? '#67C23A' : '#F56C6C');

onMounted(async () => {
  // 从 sessionStorage 读取答题结果
  const data = sessionStorage.getItem(`examResult_${stageId.value}`);
  if (data) {
    const parsed = JSON.parse(data);
    stageName.value = parsed.stageName;
    score.value = parsed.score;
    totalScore.value = parsed.totalScore;
    results.value = parsed.results;
  } else {
    ElMessage.warning('未找到答题记录');
    router.back();
  }
});

const goBack = () => router.push('/student/stages');

const goNext = async () => {
  loading.value = true;
  try {
    const res = await getStudentStages();
    const stages = res.data || [];
    const nextStage = stages.find((s: any) => s.id === stageId.value + 1);
    if (nextStage && nextStage.unlocked) {
      router.push(`/student/exam/${nextStage.id}`);
    } else {
      ElMessage.success('你已完成所有关卡！');
      router.push('/student/stages');
    }
  } catch {
    router.push('/student/stages');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.student-result-page { padding: 0; }
.result-header { text-align: center; padding: 30px 0; }
.result-header h2 { margin: 16px 0 4px 0; }
.result-header p { color: #909399; }
.score-display { display: flex; justify-content: center; margin: 20px 0; }
.score-text { font-size: 36px; font-weight: 700; }
.score-total { font-size: 16px; color: #909399; }
.result-stats { margin: 24px 0; }
.stat-card { text-align: center; padding: 16px 0; }
.stat-value { font-size: 32px; font-weight: 700; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
.detail-section { margin-top: 24px; }
.detail-section h4 { margin-bottom: 16px; }
.detail-item { padding: 12px; border-radius: 8px; margin-bottom: 8px; }
.detail-item.correct { background: #f0f9eb; border-left: 4px solid #67C23A; }
.detail-item.wrong { background: #fef0f0; border-left: 4px solid #F56C6C; }
.detail-item p { margin: 6px 0; }
.explanation-box { margin-top: 8px; padding: 16px; background: #f0f7ff; border: 1px solid #c6e2ff; border-radius: 8px; }
.explanation-header { display: flex; align-items: center; gap: 6px; margin-bottom: 10px; color: #409EFF; }
.explanation-content { font-size: 14px; line-height: 1.8; color: #303133; }
.result-actions { text-align: center; margin-top: 24px; display: flex; justify-content: center; gap: 12px; }
</style>
