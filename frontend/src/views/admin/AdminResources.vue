<template>
  <div class="admin-resources-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>资源管理</h3>
          <p>管理教学资源库中的所有资源文件</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="showAdd = true">
            <el-icon><Plus /></el-icon> 上传资源
          </el-button>
        </div>
      </div>

      <el-table :data="resources" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="资源名称" width="250" show-overflow-tooltip />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.resourceType)">{{ (row.resourceType || '').toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <el-tag v-for="tag in (row.tags || [])" :key="tag" size="small" style="margin:2px">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadedBy" label="上传者" width="100" />
        <el-table-column prop="uploadDate" label="上传日期" width="120" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="previewResource(row)">
              <el-icon><View /></el-icon> 预览
            </el-button>
            <el-button size="small" type="success" link @click="handleDownload(row)">
              <el-icon><Download /></el-icon> 下载
            </el-button>
            <el-button size="small" type="danger" link @click="deleteResource(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="上传资源" width="500px" :close-on-click-modal="false">
      <el-form :model="resourceForm" label-width="80px">
        <el-form-item label="资源名称" required>
          <el-input v-model="resourceForm.title" />
        </el-form-item>
        <el-form-item label="资源类型" required>
          <el-select v-model="resourceForm.resourceType">
            <el-option label="PDF" value="pdf" />
            <el-option label="视频" value="video" />
            <el-option label="文档" value="doc" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="resourceForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="resourceForm.tagsInput" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-form-item label="文件URL">
          <el-input v-model="resourceForm.fileUrl" placeholder="请输入资源文件URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd">确定上传</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPreview" :title="previewTitle" width="80%" top="5vh">
      <div class="pdf-preview-container">
        <div class="pdf-placeholder">
          <el-icon :size="64"><Document /></el-icon>
          <p>{{ previewTitle }}</p>
          <p style="color:#909399">{{ previewDesc }}</p>
          <el-button type="primary" @click="openPdfWindow">在新窗口打开PDF</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Document, View, Download } from '@element-plus/icons-vue';
import { getAdminResources, createResource, deleteResource } from '../../api';
import { previewPdf, downloadResource as downloadApi } from '../../api';

const loading = ref(false);
const resources = ref<any[]>([]);
const showAdd = ref(false);
const showPreview = ref(false);
const previewTitle = ref('');
const previewDesc = ref('');

const resourceForm = ref({
  title: '',
  resourceType: 'pdf' as string,
  description: '',
  tagsInput: '',
  fileUrl: '',
});

const typeTag = (type: string) => {
  const map: Record<string, any> = { pdf: 'danger', video: 'success', doc: 'warning' };
  return map[type] || 'info';
};

const loadResources = async () => {
  loading.value = true;
  try {
    const res = await getAdminResources();
    resources.value = res.data || [];
  } catch (e) {
    console.error('加载资源失败', e);
    ElMessage.error('加载资源列表失败');
  } finally {
    loading.value = false;
  }
};

const confirmAdd = async () => {
  if (!resourceForm.value.title) {
    ElMessage.warning('请输入资源名称');
    return;
  }
  try {
    await createResource({
      title: resourceForm.value.title,
      resourceType: resourceForm.value.resourceType,
      description: resourceForm.value.description,
      tags: resourceForm.value.tagsInput.split(',').map(t => t.trim()).filter(Boolean),
      fileUrl: resourceForm.value.fileUrl || '/api/resource/pdf',
      uploadedBy: localStorage.getItem('currentFullName') || '管理员',
      uploadDate: new Date().toISOString().split('T')[0],
    });
    ElMessage.success('资源上传成功');
    showAdd.value = false;
    resourceForm.value = { title: '', resourceType: 'pdf', description: '', tagsInput: '', fileUrl: '' };
    loadResources();
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '上传失败');
  }
};

const previewResource = (row: any) => {
  previewTitle.value = row.title;
  previewDesc.value = row.description || '';
  showPreview.value = true;
};

const openPdfWindow = async () => {
  try {
    const res = await previewPdf();
    const blob = new Blob([res.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    window.open(url);
    window.URL.revokeObjectURL(url);
  } catch {
    ElMessage.warning('PDF加载失败，请检查网络连接');
  }
};

const handleDownload = async (row: any) => {
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
    ElMessage.success(`${row.title} 下载成功`);
  } catch {
    ElMessage.warning('下载失败，请重试');
  }
};

const deleteResource = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除"${row.title}"吗？`, '删除确认', { type: 'warning' });
    await deleteResource(row.id);
    ElMessage.success('删除成功');
    loadResources();
  } catch { /* cancelled */ }
};

onMounted(loadResources);
</script>

<style scoped>
.admin-resources-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.header-actions { display: flex; gap: 8px; }
.pdf-preview-container { text-align: center; padding: 40px; }
.pdf-placeholder { padding: 60px 0; }
.pdf-placeholder p { margin: 12px 0; font-size: 16px; }
</style>
