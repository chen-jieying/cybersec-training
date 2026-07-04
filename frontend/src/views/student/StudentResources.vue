<template>
  <div class="student-resources-page">
    <el-card shadow="hover">
      <div class="page-header">
        <h3>教学资源</h3>
        <p>浏览和学习网络安全相关的教学资源</p>
      </div>

      <el-row :gutter="16">
        <el-col v-for="res in resources" :key="res.id" :span="6" style="margin-bottom:16px">
          <el-card shadow="hover" class="resource-card">
            <div class="resource-type">
              <el-tag :type="res.resourceType === 'pdf' ? 'danger' : res.resourceType === 'video' ? 'success' : 'warning'" size="small">
                {{ res.resourceType.toUpperCase() }}
              </el-tag>
            </div>
            <h4 class="resource-title" @click="previewResource(res)">{{ res.title }}</h4>
            <p class="resource-desc">{{ res.description }}</p>
            <div class="resource-tags">
              <el-tag v-for="tag in res.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
            </div>
            <div class="resource-footer">
              <el-button size="small" type="primary" @click="previewResource(res)">
                <el-icon><View /></el-icon> 预览
              </el-button>
              <el-button size="small" type="success" @click="downloadResource(res)">
                <el-icon><Download /></el-icon> 下载
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 预览弹窗 -->
    <el-dialog v-model="showPreview" :title="previewTitle" width="80%" top="5vh">
      <div class="preview-container">
        <div style="text-align:center;padding:60px;">
          <el-icon :size="64" color="#409EFF"><Document /></el-icon>
          <p style="font-size:18px;margin-top:16px;">{{ previewTitle }}</p>
          <p style="color:#909399;">{{ previewDesc }}</p>
          <el-button type="primary" @click="openPdfPreview">在新窗口打开预览</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Document, View, Download } from '@element-plus/icons-vue';
import { mockResources, TeachingResource } from '../../mock/data';
import { previewPdf } from '../../api';

const resources = ref<TeachingResource[]>(mockResources);
const showPreview = ref(false);
const previewTitle = ref('');
const previewDesc = ref('');

const previewResource = (row: TeachingResource) => {
  previewTitle.value = row.title;
  previewDesc.value = row.description;
  showPreview.value = true;
};

/** 下载教学资源 */
const downloadResource = (row: TeachingResource) => {
  try {
    const type = row.resourceType;
    const content = `标题: ${row.title}\n类型: ${type}\n描述: ${row.description}\n标签: ${row.tags?.join(', ')}\n\n网络安全素养实训平台`;
    const blob = type === 'pdf'
      ? new Blob([content], { type: 'application/pdf' })
      : new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${row.title}.${type === 'pdf' ? 'pdf' : 'txt'}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    ElMessage.success('下载成功');
  } catch {
    ElMessage.warning('下载失败');
  }
};

const openPdfPreview = async () => {
  try {
    const res = await previewPdf();
    const blob = new Blob([res.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    window.open(url);
    window.URL.revokeObjectURL(url);
  } catch {
    ElMessage.warning('预览失败');
  }
};
</script>

<style scoped>
.student-resources-page { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.resource-card { cursor: default; transition: transform 0.2s; height: 100%; }
.resource-card:hover { transform: translateY(-2px); }
.resource-type { margin-bottom: 12px; }
.resource-title { margin: 0 0 8px 0; font-size: 15px; color: #409EFF; cursor: pointer; }
.resource-title:hover { text-decoration: underline; }
.resource-desc { font-size: 13px; color: #606266; margin-bottom: 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.resource-tags { display: flex; gap: 4px; flex-wrap: wrap; margin-bottom: 12px; }
.resource-footer { display: flex; gap: 8px; justify-content: center; padding-top: 8px; border-top: 1px solid #ebeef5; }
.preview-container { text-align: center; padding: 40px; }
</style>
