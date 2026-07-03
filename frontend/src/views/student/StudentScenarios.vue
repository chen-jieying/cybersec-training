<template>
  <div class="student-scenarios-page">
    <el-card shadow="hover">
      <div class="page-header">
        <h3>AI情景仿真实训</h3>
        <p>选择一个安全情景，与AI进行多轮对话，学习如何应对各种网络安全威胁</p>
      </div>

      <el-row :gutter="20">
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
            <el-tag
              :type="scenario.difficulty === 'easy' ? 'success' : scenario.difficulty === 'medium' ? 'warning' : 'danger'"
              size="small"
            >
              {{ scenario.difficulty === 'easy' ? '简单' : scenario.difficulty === 'medium' ? '中等' : '困难' }}
            </el-tag>
            <p class="scenario-desc">{{ scenario.description }}</p>
            <el-button type="primary" plain style="width:100%;margin-top:12px;">开始实训</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Message, Lock, Phone, ChatDotRound, Warning, Connection } from '@element-plus/icons-vue';
import { mockScenarios, ScenarioScript } from '../../mock/data';

const router = useRouter();
const scenarios = ref<ScenarioScript[]>(mockScenarios);

const enterScenario = (scenario: ScenarioScript) => {
  router.push(`/student/chat/${scenario.id}`);
};
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
.scenario-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(64,158,255,0.15);
}
.scenario-icon { margin-bottom: 12px; }
.scenario-title { margin: 0 0 8px 0; font-size: 16px; }
.scenario-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 12px;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
