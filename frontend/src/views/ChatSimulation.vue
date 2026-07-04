<template>
  <div class="simulation-page">
    <el-card shadow="hover">
      <div class="header-row">
        <div>
          <h3>诈骗情景对话实训</h3>
          <p>请与系统展开多轮模拟对话，识别诈骗意图并选择正确应对。</p>
        </div>
        <div class="header-actions">
          <el-button v-if="isTeacher" type="success" @click="goChatRecords">
            <el-icon><View /></el-icon> 查看学生对话记录
          </el-button>
          <el-button type="primary" @click="resetChat">重新开始</el-button>
        </div>
      </div>
      <chat-simulator />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { View } from '@element-plus/icons-vue';
import ChatSimulator from '../components/ChatSimulator.vue';

const router = useRouter();

const isTeacher = computed(() => localStorage.getItem('currentRole') === 'teacher');

const resetChat = () => {
  window.location.reload();
};

/** 教师跳转到班级学生对话记录页面 */
const goChatRecords = () => {
  router.push('/teacher/chat');
};
</script>

<style scoped>
.simulation-page {
  padding: 20px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.header-actions {
  display: flex;
  gap: 8px;
}
</style>
