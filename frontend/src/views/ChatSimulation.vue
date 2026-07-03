<template>
  <div class="simulation-page">
    <el-card shadow="hover">
      <div class="header-row">
        <div>
          <h3>诈骗情景对话实训</h3>
          <p>请与系统展开多轮模拟对话，识别诈骗意图并选择正确应对。</p>
        </div>
        <el-button type="primary" @click="resetChat">重新开始</el-button>
      </div>
      <chat-simulator />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import ChatSimulator from '../components/ChatSimulator.vue';

const resetChat = () => {
  window.location.reload();
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
}
</style>
