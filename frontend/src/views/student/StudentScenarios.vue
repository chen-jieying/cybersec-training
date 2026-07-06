<template>
  <div class="student-scenarios-page">
    <el-card shadow="hover">
      <div class="page-header">
        <h3>AI情景仿真实训</h3>
        <p>选择一个安全情景，与AI进行多轮对话，学习如何应对各种网络安全威胁</p>
      </div>

      <el-row :gutter="20" v-loading="loading">
        <el-col v-for="scenario in scenarios" :key="scenario.id" :span="8">
          <el-card shadow="hover" class="scenario-card" @click="enterScenario(scenario)">
            <div class="scenario-icon">
              <el-icon :size="36" color="#409EFF">
                <Message v-if="scenario.icon === 'Message'" />
                <Lock v-else-if="scenario.icon === 'Lock'" />
                <Phone v-else-if="scenario.icon === 'Phone'" />
                <ChatDotRound v-else-if="scenario.icon === 'ChatDotRound'" />
                <Warning v-else-if="scenario.icon === 'Warning'" />
                <Connection v-else />
              </el-icon>
            </div>
            <h4 class="scenario-title">{{ scenario.title }}</h4>
            <div class="scenario-tags">
              <el-tag
                :type="scenario.difficulty === 'easy' ? 'success' : scenario.difficulty === 'medium' ? 'warning' : 'danger'"
                size="small"
              >
                {{ scenario.difficulty === 'easy' ? '简单' : scenario.difficulty === 'medium' ? '中等' : '困难' }}
              </el-tag>
              <el-tag v-if="isCompleted(scenario.id)" type="success" size="small" effect="dark">
                已完成
              </el-tag>
            </div>
            <p class="scenario-desc">{{ scenario.description }}</p>
            <div v-if="getStatus(scenario.id)" class="scenario-status">
              <span v-if="getStatus(scenario.id).status === '待教师评分'">
                状态：等待教师评分
              </span>
              <span v-else>
                评分：{{ getStatus(scenario.id).score }} 分
              </span>
            </div>
            <el-button type="primary" plain style="width:100%;margin-top:12px;">
              {{ isCompleted(scenario.id) ? '再做一次' : '开始实训' }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && scenarios.length === 0" description="暂无情景剧本" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Message, Lock, Phone, ChatDotRound, Warning, Connection } from '@element-plus/icons-vue';
import { getStudentScenarios } from '../../api';
import api from '../../api';

const router = useRouter();
const loading = ref(false);
const scenarios = ref<any[]>([]);
const trainingStatus = ref<Map<number, any>>(new Map());

const loadScenarios = async () => {
  loading.value = true;
  try {
    const [scenarioRes, statusRes] = await Promise.all([
      getStudentScenarios(),
      api.get('/chat/training-status'),
    ]);
    scenarios.value = scenarioRes.data || [];

    // 建立状态映射
    const statusMap = new Map<number, any>();
    (statusRes.data || []).forEach((s: any) => {
      statusMap.set(s.scenarioId, s);
    });
    trainingStatus.value = statusMap;
  } catch (e) {
    console.error('加载失败', e);
  } finally {
    loading.value = false;
  }
};

const isCompleted = (scenarioId: number) => {
  return trainingStatus.value.has(scenarioId);
};

const getStatus = (scenarioId: number) => {
  return trainingStatus.value.get(scenarioId) || null;
};

const enterScenario = (scenario: any) => {
  router.push(`/student/chat/${scenario.id}`);
};

onMounted(loadScenarios);
</script>

<style scoped>
.student-scenarios-page { padding: 0; }
.page-header { margin-bottom: 24px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.scenario-card {
  cursor: pointer;
  transition: transform 0.2s;
  text-align: center;
  padding: 16px;
  height: 100%;
}
.scenario-card:hover { transform: translateY(-4px); box-shadow: 0 4px 16px rgba(64,158,255,0.15); }
.scenario-icon { margin-bottom: 12px; }
.scenario-title { margin: 0 0 8px 0; font-size: 16px; }
.scenario-tags { display: flex; justify-content: center; gap: 6px; margin-bottom: 8px; }
.scenario-desc { font-size: 13px; color: #909399; margin-top: 8px; min-height: 40px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.scenario-status { font-size: 12px; color: #409EFF; margin-top: 6px; }
</style>
