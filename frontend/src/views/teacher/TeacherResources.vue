<template>
  <div class="teacher-resources-page">
    <el-card shadow="hover">
      <div class="page-header">
        <h3>教学资源</h3>
        <p>浏览和预览教学资源库中的课件和资料</p>
      </div>

      <el-row :gutter="16" v-loading="loading">
        <el-col v-for="res in resources" :key="res.id" :span="8" style="margin-bottom:16px">
          <el-card shadow="hover" class="resource-card">
            <div class="resource-header">
              <el-tag :type="typeTag(res.resourceType)" size="small">{{ (res.resourceType || '').toUpperCase() }}</el-tag>
              <span class="resource-date">{{ res.uploadDate }}</span>
            </div>
            <h4 class="resource-title" @click="openPreview(res)">{{ res.title }}</h4>
            <p class="resource-desc">{{ res.description }}</p>
            <div class="resource-tags">
              <el-tag v-for="(tag, idx) in (res.tags ? res.tags.split(',').filter((t:string) => t.trim()) : [])" :key="idx" size="small" type="info">{{ tag.trim() }}</el-tag>
            </div>
            <div class="resource-footer">
              <span>上传者：{{ res.uploadedBy || '系统' }}</span>
              <div class="resource-actions">
                <el-button size="small" type="primary" @click="openPreview(res)">
                  <el-icon><View /></el-icon> 预览
                </el-button>
                <el-button size="small" type="success" @click="handleDownload(res)">
                  <el-icon><Download /></el-icon> 下载
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && resources.length === 0" description="暂无教学资源" />
    </el-card>

    <!-- 预览弹窗 - 内嵌 iframe 直接显示 -->
    <el-dialog v-model="showPreview" :title="previewTitle" width="90%" top="3vh" :close-on-click-modal="false"
      destroy-on-close>
      <div v-loading="previewLoading" style="min-height:500px;">
        <!-- PDF 直接嵌入显示 -->
        <iframe v-if="previewType === 'pdf'" :src="previewUrl" width="100%" height="700px"
          style="border:none;border-radius:4px;" @load="previewLoading = false" />
        <!-- 其他类型显示 HTML -->
        <iframe v-else :srcdoc="previewHtml" width="100%" height="700px"
          style="border:none;border-radius:4px;" @load="previewLoading = false" />
      </div>
      <template #footer>
        <el-button @click="showPreview = false">关闭</el-button>
        <el-button type="success" @click="handleDownload(currentPreviewResource)">
          <el-icon><Download /></el-icon> 下载此资源
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { View, Download } from '@element-plus/icons-vue';
import { getResourceList, downloadResource as downloadApi, previewResource as previewApi } from '../../api';

const loading = ref(false);
const resources = ref<any[]>([]);

const showPreview = ref(false);
const previewLoading = ref(false);
const previewTitle = ref('');
const previewType = ref('pdf');
const previewUrl = ref('');
const previewHtml = ref('');
const currentPreviewResource = ref<any>(null);

const typeTag = (type: string) => {
  const map: Record<string, any> = { pdf: 'danger', video: 'success', doc: 'warning' };
  return map[type] || 'info';
};

const loadResources = async () => {
  loading.value = true;
  try {
    const res = await getResourceList();
    resources.value = res.data || [];
  } catch (e) {
    console.error('加载资源失败', e);
  } finally {
    loading.value = false;
  }
};

const openPreview = async (row: any) => {
  currentPreviewResource.value = row;
  previewTitle.value = row.title;
  previewType.value = row.resourceType || 'doc';
  previewLoading.value = true;

  if (previewType.value === 'pdf') {
    // PDF 类型通过后端API直接在iframe中显示
    previewUrl.value = `/api/resource/preview/${row.id}`;
    // 设置超时隐藏 loading
    setTimeout(() => { previewLoading.value = false; }, 2000);
  } else {
    // 视频和文档获取HTML内容嵌入
    try {
      const res = await previewApi(row.id);
      previewHtml.value = typeof res.data === 'string' ? res.data : '';
    } catch {
      previewHtml.value = `<html><body style="padding:40px;text-align:center;"><h2>${row.title}</h2><p>${row.description || ''}</p></body></html>`;
    }
    previewLoading.value = false;
  }
  showPreview.value = true;
};

const handleDownload = async (row: any) => {
  if (!row) return;
  try {
    const res = await downloadApi(row.id);
    const blob = new Blob([res.data]);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${row.title}.${row.resourceType || 'pdf'}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    ElMessage.success('下载成功');
  } catch {
    ElMessage.warning('下载失败');
  }
};

onMounted(loadResources);
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
.resource-actions { display: flex; gap: 8px; }
</style>
