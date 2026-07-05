<template>
  <div class="teacher-resources-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>教学资源</h3>
          <p>管理教学资源，上传资源时可选择对哪些班级可见</p>
        </div>
        <el-button type="primary" @click="showUpload = true">
          <el-icon><Upload /></el-icon> 上传资源
        </el-button>
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
            <div style="margin-bottom:8px;">
              <el-tag v-if="res.uploaderRole === 'ADMIN'" type="danger" size="small" effect="plain">管理员资源（全局可见）</el-tag>
              <el-tag v-else type="warning" size="small" effect="plain">
                {{ res.visibility === 'ALL' ? '全部班级可见' : res.visibility === 'SPECIFIC_CLASSES' ? '指定班级可见' : '仅我所管班级' }}
              </el-tag>
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

    <!-- 上传资源弹窗 -->
    <el-dialog v-model="showUpload" title="上传教学资源" width="550px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="资源文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".pdf,.doc,.docx,.mp4,.avi,.mov"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon> 选择文件
            </el-button>
            <template #tip>
              <div style="font-size:12px;color:#909399;margin-top:4px;">支持 PDF、Word 文档、视频文件</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="资源标题" required>
          <el-input v-model="uploadForm.title" placeholder="请输入资源标题" />
        </el-form-item>
        <el-form-item label="资源描述">
          <el-input v-model="uploadForm.description" type="textarea" :rows="2" placeholder="请输入资源描述" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="uploadForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="可见范围" required>
          <el-radio-group v-model="uploadForm.visibility">
            <el-radio value="ALL">全部班级</el-radio>
            <el-radio value="SPECIFIC_CLASSES">指定班级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择班级" v-if="uploadForm.visibility === 'SPECIFIC_CLASSES'">
          <el-select v-model="uploadForm.visibleClassIds" multiple placeholder="请选择可见班级" style="width:100%">
            <el-option v-for="cls in myClasses" :key="cls.id" :label="cls.className" :value="String(cls.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="可见说明" v-if="uploadForm.visibility !== 'SPECIFIC_CLASSES'">
          <span style="color:#909399;font-size:13px;">
            您上传的资源仅对您所管理的班级学生可见，管理员上传的资源全部学生可见
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUpload = false">取消</el-button>
        <el-button type="primary" @click="confirmUpload" :loading="uploading">确认上传</el-button>
      </template>
    </el-dialog>

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
import { View, Download, Upload } from '@element-plus/icons-vue';
import {
  getResourceList, downloadResource as downloadApi,
  previewResource as previewApi, uploadResource,
  getTeacherClasses
} from '../../api';

const loading = ref(false);
const resources = ref<any[]>([]);

const showUpload = ref(false);
const uploading = ref(false);
const uploadForm = ref({
  title: '', description: '', tags: '',
  visibility: 'ALL' as string,
  visibleClassIds: [] as string[]
});
const selectedFile = ref<File | null>(null);
const myClasses = ref<any[]>([]);

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

const loadMyClasses = async () => {
  try {
    const res = await getTeacherClasses();
    myClasses.value = res.data || [];
  } catch { /* */ }
};

const handleFileChange = (file: any) => {
  selectedFile.value = file.raw || null;
  if (!uploadForm.value.title) {
    uploadForm.value.title = file.name.replace(/\.[^.]+$/, '');
  }
};

const handleFileRemove = () => {
  selectedFile.value = null;
};

const confirmUpload = async () => {
  if (!selectedFile.value) { ElMessage.warning('请选择文件'); return; }
  if (!uploadForm.value.title.trim()) { ElMessage.warning('请输入资源标题'); return; }

  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);
    formData.append('title', uploadForm.value.title);
    formData.append('description', uploadForm.value.description);
    formData.append('tags', uploadForm.value.tags);
    formData.append('visibility', uploadForm.value.visibility);
    if (uploadForm.value.visibility === 'SPECIFIC_CLASSES') {
      formData.append('visibleClassIds', uploadForm.value.visibleClassIds.join(','));
    }

    await uploadResource(formData);
    ElMessage.success('上传成功');
    showUpload.value = false;
    uploadForm.value = { title: '', description: '', tags: '', visibility: 'ALL', visibleClassIds: [] };
    selectedFile.value = null;
    loadResources();
  } catch {
    ElMessage.error('上传失败');
  } finally {
    uploading.value = false;
  }
};

const openPreview = async (row: any) => {
  currentPreviewResource.value = row;
  previewTitle.value = row.title;
  previewType.value = row.resourceType || 'doc';
  previewLoading.value = true;

  if (previewType.value === 'pdf') {
    previewUrl.value = `/api/resource/preview/${row.id}`;
    setTimeout(() => { previewLoading.value = false; }, 2000);
  } else {
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

onMounted(() => {
  loadResources();
  loadMyClasses();
});
</script>

<style scoped>
.teacher-resources-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
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
