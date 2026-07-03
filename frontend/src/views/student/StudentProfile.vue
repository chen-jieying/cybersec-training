<template>
  <div class="student-profile-page">
    <el-card shadow="hover">
      <div class="page-header">
        <h3>个人中心</h3>
        <p>查看个人成绩、错题记录和素养评估</p>
      </div>

      <!-- 基本信息 -->
      <el-descriptions :column="3" border style="margin-bottom:20px;">
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userInfo.fullName }}</el-descriptions-item>
        <el-descriptions-item label="角色">学生</el-descriptions-item>
        <el-descriptions-item label="年级">{{ userInfo.grade }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ userInfo.className }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ userInfo.studentNo }}</el-descriptions-item>
      </el-descriptions>

      <!-- Tab切换 -->
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 得分记录 -->
        <el-tab-pane label="得分记录" name="scores">
          <el-table :data="scoreHistory" border stripe>
            <el-table-column prop="date" label="日期" width="150" />
            <el-table-column prop="stage" label="关卡" width="120" />
            <el-table-column prop="score" label="得分" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.score >= 80 ? 'success' : row.score >= 60 ? 'warning' : 'danger'">
                  {{ row.score }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="得分率" min-width="200">
              <template #default="{ row }">
                <el-progress :percentage="row.score" :color="row.score >= 80 ? '#67C23A' : row.score >= 60 ? '#E6A23C' : '#F56C6C'" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 错题记录 -->
        <el-tab-pane label="错题记录" name="wrong">
          <el-empty v-if="wrongQuestions.length === 0" description="暂无错题记录" />
          <div v-else class="wrong-questions">
            <el-card v-for="q in wrongQuestions" :key="q.id" shadow="hover" class="wrong-card">
              <div class="wrong-header">
                <el-tag size="small">{{ q.category }}</el-tag>
                <span class="wrong-date">{{ q.date }}</span>
              </div>
              <p class="wrong-text">{{ q.questionText }}</p>
              <div class="wrong-answers">
                <span class="wrong-answer">
                  你的答案：<el-tag type="danger" size="small">{{ q.userAnswer }}</el-tag>
                </span>
                <span class="correct-answer">
                  正确答案：<el-tag type="success" size="small">{{ q.correctAnswer }}</el-tag>
                </span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 素养雷达图 -->
        <el-tab-pane label="素养评估" name="radar">
          <div class="radar-container">
            <echart-radar :data="radarData" />
          </div>
          <el-row :gutter="16" style="margin-top:20px;">
            <el-col v-for="(item, index) in radarDetails" :key="index" :span="Math.floor(24 / radarDetails.length)">
              <el-card shadow="hover" class="radar-detail-card">
                <div class="detail-value">{{ item.score }}</div>
                <div class="detail-label">{{ item.label }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import EchartRadar from '../../components/EchartRadar.vue';
import { mockWrongQuestions, mockScoreHistory } from '../../mock/data';

const activeTab = ref('scores');

const userInfo = ref({
  username: 'student1',
  fullName: '张三',
  grade: '初二',
  className: '初二(2)班',
  studentNo: '20240001',
});

const scoreHistory = ref(mockScoreHistory);
const wrongQuestions = ref(mockWrongQuestions);

const radarData = ref({
  labels: ['安全意识', '钓鱼识别', '密码管理', '隐私保护', '应急响应'],
  scores: [75, 82, 68, 70, 65],
});

const radarDetails = ref([
  { label: '安全意识', score: 75 },
  { label: '钓鱼识别', score: 82 },
  { label: '密码管理', score: 68 },
  { label: '隐私保护', score: 70 },
  { label: '应急响应', score: 65 },
]);
</script>

<style scoped>
.student-profile-page { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.wrong-questions { display: flex; flex-direction: column; gap: 12px; }
.wrong-card { padding: 8px; }
.wrong-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.wrong-date { font-size: 12px; color: #909399; }
.wrong-text { font-size: 14px; margin-bottom: 10px; line-height: 1.6; }
.wrong-answers { display: flex; gap: 24px; font-size: 13px; }
.radar-container { max-width: 500px; margin: 0 auto; }
.radar-detail-card { text-align: center; padding: 12px 0; }
.detail-value { font-size: 28px; font-weight: 700; color: #409EFF; }
.detail-label { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
