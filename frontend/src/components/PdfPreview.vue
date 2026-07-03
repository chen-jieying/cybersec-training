<template>
  <div class="pdf-preview">
    <el-card shadow="hover">
      <div class="pdf-head">
        <div>
          <h4>教学资源预览</h4>
          <p>点击按钮在新页面查看 PDF 教学资料。</p>
        </div>
        <el-button type="primary" @click="openPdf">预览 PDF</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { previewPdf } from '../api';

const openPdf = async () => {
  const res = await previewPdf();
  const blob = new Blob([res.data], { type: 'application/pdf' });
  const url = window.URL.createObjectURL(blob);
  window.open(url, '_blank');
};
</script>

<style scoped>
.pdf-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
