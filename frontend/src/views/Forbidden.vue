<template>
  <div class="forbidden-page">
    <el-card class="forbidden-card">
      <h2>403 无权限</h2>
      <p>您没有权限访问该页面。</p>
      <el-button type="primary" @click="goBack">返回</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
const router = useRouter();
const goBack = () => router.back();
</script>

<style scoped>
.forbidden-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 60px);
}
.forbidden-card { width: 420px; text-align: center; }
</style>
