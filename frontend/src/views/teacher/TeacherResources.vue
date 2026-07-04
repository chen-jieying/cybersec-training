<template>
  <div class="teacher-resources-page">
    <el-card shadow="hover">
      <div class="page-header">
        <h3>教学资源</h3>
        <p>浏览和预览教学资源库中的课件和资料</p>
      </div>

      <el-row :gutter="16">
        <el-col v-for="res in resources" :key="res.id" :span="8" style="margin-bottom:16px">
          <el-card shadow="hover" class="resource-card">
            <div class="resource-header">
              <el-tag :type="typeTag(res.resourceType)" size="small">{{ res.resourceType.toUpperCase() }}</el-tag>
              <span class="resource-date">{{ res.uploadDate }}</span>
            </div>
            <h4 class="resource-title" @click="previewResource(res)">{{ res.title }}</h4>
            <p class="resource-desc">{{ res.description }}</p>
            <div class="resource-tags">
              <el-tag v-for="tag in res.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
            </div>
            <div class="resource-footer">
              <span>上传者：{{ res.uploadedBy }}</span>
              <div class="resource-actions">
                <el-button size="small" type="primary" link @click="previewResource(res)">
                  <el-icon><View /></el-icon> 预览
                </el-button>
                <el-button size="small" type="success" link @click="downloadResource(res)">
                  <el-icon><Download /></el-icon> 下载
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- PDF预览弹窗 -->
    <el-dialog v-model="showPreview" :title="previewTitle" width="80%" top="5vh">
      <div class="pdf-preview-container">
        <div class="pdf-placeholder">
          <el-icon :size="64" color="#409EFF"><Document /></el-icon>
          <p style="font-size:18px;margin-top:16px;">{{ previewTitle }}</p>
          <p style="color:#909399;">{{ previewDesc }}</p>
          <el-button type="primary" @click="openPdfWindow">在新窗口打开PDF预览</el-button>
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

const typeTag = (type: string) => {
  const map: Record<string, any> = { pdf: 'danger', video: 'success', doc: 'warning' };
  return map[type] || 'info';
};

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

const openPdfWindow = async () => {
  try {
    const res = await previewPdf();
    const blob = new Blob([res.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    window.open(url);
    window.URL.revokeObjectURL(url);
  } catch {
    ElMessage.warning('PDF加载失败');
  }
};
</script>

<style scoped>
.teacher-resources-page { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.resource-card { cursor: default; transition: transform 0.2s; }
.resource-card:hover { transform: translateY(-2px); }
.resource-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.resource-date { font-size: 12px; color: #909399; }
.resource-title { margin: 0 0 8px 0; font-size: 16px; cursor: pointer; color: #409EFF; }
.resource-title:hover { text-decoration: underline; }
.resource-desc { font-size: 13px; color: #606266; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.resource-tags { margin-bottom: 12px; display: flex; gap: 4px; flex-wrap: wrap; }
.resource-footer { display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #909399; border-top: 1px solid #ebeef5; padding-top: 12px; }
.resource-actions { display: flex; gap: 4px; }
.pdf-preview-container { text-align: center; padding: 40px; }
.pdf-placeholder { padding: 60px 0; }
.pdf-placeholder p { margin: 12px 0; }
</style>
