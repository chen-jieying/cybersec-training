<template>
  <div class="student-resources-page">
    <el-card shadow="hover">
      <div class="page-header">
        <h3>教学资源</h3>
        <p>浏览和学习网络安全相关的教学资源，包括管理员和本班教师上传的资料</p>
      </div>

      <el-row :gutter="16" v-loading="loading">
        <el-col v-for="res in resources" :key="res.id" :span="6" style="margin-bottom:16px">
          <el-card shadow="hover" class="resource-card">
            <div class="resource-type">
              <el-tag :type="typeTag(res.resourceType)" size="small">
                {{ (res.resourceType || '').toUpperCase() }}
              </el-tag>
              <el-tag v-if="res.uploaderRole === 'ADMIN'" type="danger" size="small" effect="plain" style="margin-left:4px;">官方</el-tag>
            </div>
            <h4 class="resource-title" @click="previewResource(res)">{{ res.title }}</h4>
            <p class="resource-desc">{{ res.description }}</p>
            <div class="resource-tags">
              <el-tag v-for="(tag, idx) in (res.tags ? res.tags.split(',').filter((t:string) => t.trim()) : [])" :key="idx" size="small" type="info">{{ tag.trim() }}</el-tag>
            </div>
            <div class="resource-footer">
              <span style="font-size:12px;color:#909399;">{{ res.uploadDate || '' }}</span>
              <div style="display:flex;gap:8px;">
                <el-button size="small" type="primary" @click="previewResource(res)">
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

    <!-- 预览弹窗 -->
    <el-dialog v-model="showPreview" :title="previewTitle" width="90%" top="3vh" :close-on-click-modal="false" destroy-on-close>
      <div v-loading="previewLoading" style="min-height:500px;">
        <iframe v-if="previewType === 'pdf'" :src="previewUrl" width="100%" height="700px"
          style="border:none;border-radius:4px;" @load="previewLoading = false" />
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
import { getResourceList, previewPdf, downloadResource as downloadApi, previewResource as previewApi } from '../../api';

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

const previewResource = async (row: any) => {
  currentPreviewResource.value = row;
  previewTitle.value = row.title;
  previewType.value = row.resourceType || 'doc';
  previewLoading.value = true;
  showPreview.value = true;

  try {
    const res = await previewApi(row.id);
    const blob = new Blob([res.data], { type: res.headers['content-type'] || 'application/octet-stream' });
    const url = window.URL.createObjectURL(blob);

    if (previewType.value === 'pdf') {
      // 用 blob URL 在新窗口打开PDF，避免iframe跨域问题
      previewUrl.value = url;
    } else {
      // 非PDF用iframe展示HTML
      const text = await blob.text();
      previewHtml.value = text;
    }
  } catch (e) {
    if (previewType.value === 'pdf') {
      previewUrl.value = '';
    }
    previewHtml.value = `<html><body style="padding:40px;text-align:center;"><h2>${row.title}</h2><p>${row.description || ''}</p><p style="color:#909399">预览加载失败，请尝试下载后查看</p></body></html>`;
  }
  previewLoading.value = false;
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

onMounted(loadResources);
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
.resource-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 8px; border-top: 1px solid #ebeef5; }
</style>
