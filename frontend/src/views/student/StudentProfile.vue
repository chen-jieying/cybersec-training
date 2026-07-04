<template>
  <div class="student-profile-page">
    <el-card shadow="hover" v-loading="loading">
      <div class="page-header">
        <h3>个人中心</h3>
        <p>查看个人成绩、错题记录和素养评估</p>
      </div>

      <!-- 基本信息 -->
      <el-descriptions :column="3" border style="margin-bottom:20px;">
        <el-descriptions-item label="用户名">{{ profile.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.fullName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">学生</el-descriptions-item>
        <el-descriptions-item label="年级">{{ profile.grade || '-' }}</el-descriptions-item>
        <el-descriptions-item label="答题总数">{{ profile.totalQuestions || 0 }} 题</el-descriptions-item>
        <el-descriptions-item label="正确率">{{ profile.accuracy || 0 }}%</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 得分记录 -->
        <el-tab-pane label="得分记录" name="scores">
          <el-row :gutter="16" style="margin-bottom:16px;">
            <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ profile.totalScore || 0 }}</div><div class="summary-label">累计得分</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ profile.totalQuestions || 0 }}</div><div class="summary-label">总答题数</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value" style="color:#67C23A">{{ profile.correctQuestions || 0 }}</div><div class="summary-label">正确题数</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value" style="color:#F56C6C">{{ profile.wrongQuestions || 0 }}</div><div class="summary-label">错误题数</div></el-card></el-col>
          </el-row>
          <el-empty v-if="(profile.totalQuestions || 0) === 0" description="暂无答题记录，请先完成闯关答题" />
        </el-tab-pane>

        <!-- 错题记录 -->
        <el-tab-pane label="错题记录" name="wrong">
          <el-empty v-if="wrongQuestions.length === 0" description="暂无错题记录，继续保持！" />
          <div v-else class="wrong-questions">
            <el-card v-for="q in wrongQuestions" :key="q.id" shadow="hover" class="wrong-card">
              <div class="wrong-header"><el-tag size="small">{{ q.category }}</el-tag><span class="wrong-date">{{ q.date }}</span></div>
              <p class="wrong-text">{{ q.questionText }}</p>
              <div class="wrong-answers">
                <span class="wrong-answer">你的答案：<el-tag type="danger" size="small">{{ q.userAnswer }}</el-tag></span>
                <span class="correct-answer">正确答案：<el-tag type="success" size="small">{{ q.correctAnswer }}</el-tag></span>
              </div>
              <div v-if="q.explanation" class="wrong-explanation">
                <el-icon color="#409EFF" :size="14"><InfoFilled /></el-icon>
                <span>{{ q.explanation }}</span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 素养评估 -->
        <el-tab-pane label="素养评估" name="radar">
          <div class="radar-container">
            <echart-radar :data="radarData" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import EchartRadar from '../../components/EchartRadar.vue';
import { getStudentProfile } from '../../api';

const activeTab = ref('scores');
const loading = ref(false);

const profile = ref<any>({
  username: '', fullName: '', grade: '',
  totalQuestions: 0, correctQuestions: 0, wrongQuestions: 0,
  accuracy: 0, totalScore: 0
});

const wrongQuestions = ref<any[]>([]);

// 雷达图数据从真实数据计算
const radarData = computed(() => {
  // 按分类统计正确率
  const catMap: Record<string, { total: number; correct: number }> = {};
  wrongQuestions.value.forEach((q: any) => {
    const cat = q.category || '其他';
    if (!catMap[cat]) catMap[cat] = { total: 0, correct: 0 };
    catMap[cat].total++;
  });
  // 加上正确计数
  const totalCorrect = profile.value.correctQuestions || 0;

  // 如果无数据则显示0
  const categories = ['基础知识', '钓鱼防护', '社交工程'];
  const scores = categories.map(() => {
    return totalCorrect > 0 ? Math.min(100, Math.round((totalCorrect / (profile.value.totalQuestions || 1)) * 100)) : 0;
  });

  return {
    labels: categories,
    scores: scores.every(s => s === 0) ? [0, 0, 0] : scores
  };
});

const loadProfile = async () => {
  loading.value = true;
  try {
    const res = await getStudentProfile();
    const data = res.data;
    profile.value = {
      username: data.user?.username || localStorage.getItem('currentUser') || '',
      fullName: data.user?.fullName || '',
      grade: data.user?.grade || '',
      totalQuestions: data.totalQuestions || 0,
      correctQuestions: data.correctQuestions || 0,
      wrongQuestions: data.wrongQuestions || 0,
      accuracy: data.accuracy || 0,
      totalScore: data.totalScore || 0
    };
    wrongQuestions.value = data.wrongQuestions || [];
  } catch {
    // 失败时保持默认值（全部为0）
    profile.value.totalQuestions = 0;
    profile.value.correctQuestions = 0;
    profile.value.wrongQuestions = 0;
    wrongQuestions.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(loadProfile);
</script>

<style scoped>
.student-profile-page { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.summary-card { text-align: center; padding: 12px 0; }
.summary-value { font-size: 28px; font-weight: 700; color: #409EFF; }
.summary-label { font-size: 13px; color: #909399; margin-top: 4px; }
.wrong-questions { display: flex; flex-direction: column; gap: 12px; }
.wrong-card { padding: 8px; }
.wrong-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.wrong-date { font-size: 12px; color: #909399; }
.wrong-text { font-size: 14px; margin-bottom: 10px; line-height: 1.6; }
.wrong-answers { display: flex; gap: 24px; font-size: 13px; }
.wrong-explanation { margin-top: 10px; padding: 10px; background: #f0f7ff; border-radius: 6px; display: flex; align-items: flex-start; gap: 6px; font-size: 13px; color: #606266; line-height: 1.6; }
.radar-container { max-width: 500px; margin: 0 auto; }
</style>
