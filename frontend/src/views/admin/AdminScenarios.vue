<template>
  <div class="admin-scenarios-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>情景剧本管理</h3>
          <p>管理AI情景仿真实训的剧本内容</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="openAddDrawer">
            <el-icon><Plus /></el-icon> 新增剧本
          </el-button>
        </div>
      </div>

      <el-table :data="scenarios" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="剧本名称" width="200" />
        <el-table-column prop="sceneType" label="场景类型" width="120">
          <template #default="{ row }">
            <el-tag :type="sceneTag(row.sceneType)">{{ row.sceneType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
        <el-table-column prop="difficulty" label="难度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="diffTag(row.difficulty)">{{ diffText(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="editScenario(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="deleteScenario(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer
      v-model="drawerVisible"
      :title="editingId ? '编辑剧本' : '新增剧本'"
      size="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="scenarioForm" label-width="100px">
        <el-form-item label="剧本名称" required>
          <el-input v-model="scenarioForm.title" placeholder="请输入剧本名称" />
        </el-form-item>
        <el-form-item label="场景类型" required>
          <el-select v-model="scenarioForm.sceneType" placeholder="请选择场景类型" style="width:100%">
            <el-option label="钓鱼攻击(phishing)" value="phishing" />
            <el-option label="账户安全(account)" value="account" />
            <el-option label="电信诈骗(fraud)" value="fraud" />
            <el-option label="社交工程(social)" value="social" />
            <el-option label="勒索软件(ransomware)" value="ransomware" />
            <el-option label="公共WiFi(wifi)" value="wifi" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" required>
          <el-radio-group v-model="scenarioForm.difficulty">
            <el-radio value="easy">简单</el-radio>
            <el-radio value="medium">中等</el-radio>
            <el-radio value="hard">困难</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="场景Prompt" required>
          <el-input
            v-model="scenarioForm.description"
            type="textarea"
            :rows="6"
            placeholder="请输入场景描述Prompt"
          />
        </el-form-item>
        <el-form-item label="打分规则">
          <el-input
            v-model="scenarioForm.scoringRule"
            type="textarea"
            :rows="4"
            placeholder="请输入AI打分规则"
          />
        </el-form-item>
        <el-form-item label="图标">
          <el-select v-model="scenarioForm.icon" placeholder="请选择图标">
            <el-option label="消息" value="Message" />
            <el-option label="锁" value="Lock" />
            <el-option label="电话" value="Phone" />
            <el-option label="聊天" value="ChatDotRound" />
            <el-option label="警告" value="Warning" />
            <el-option label="连接" value="Connection" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSave">确认保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getAdminScenarios, createScenario, updateScenario, deleteScenario } from '../../api';

const loading = ref(false);
const scenarios = ref<any[]>([]);
const drawerVisible = ref(false);
const editingId = ref<number | null>(null);

const scenarioForm = ref({
  title: '',
  sceneType: '',
  difficulty: 'easy',
  description: '',
  scoringRule: '',
  icon: 'Message',
});

const sceneTag = (type: string) => {
  const map: Record<string, any> = { phishing: 'danger', account: 'warning', fraud: 'danger', social: '', ransomware: 'danger', wifi: 'success' };
  return map[type] || 'info';
};

const diffTag = (d: string) => {
  const map: Record<string, any> = { easy: 'success', medium: 'warning', hard: 'danger' };
  return map[d] || 'info';
};

const diffText = (d: string) => {
  const map: Record<string, string> = { easy: '简单', medium: '中等', hard: '困难' };
  return map[d] || d;
};

const loadScenarios = async () => {
  loading.value = true;
  try {
    const res = await getAdminScenarios();
    scenarios.value = res.data || [];
  } catch (e) {
    console.error('加载剧本失败', e);
    ElMessage.error('加载剧本列表失败');
  } finally {
    loading.value = false;
  }
};

const openAddDrawer = () => {
  editingId.value = null;
  scenarioForm.value = { title: '', sceneType: '', difficulty: 'easy', description: '', scoringRule: '', icon: 'Message' };
  drawerVisible.value = true;
};

const editScenario = (row: any) => {
  editingId.value = row.id;
  scenarioForm.value = { ...row, scoringRule: row.scoringRule || '' };
  drawerVisible.value = true;
};

const confirmSave = async () => {
  if (!scenarioForm.value.title || !scenarioForm.value.sceneType) {
    ElMessage.warning('请填写必填信息');
    return;
  }
  try {
    if (editingId.value) {
      await updateScenario(editingId.value, scenarioForm.value);
      ElMessage.success('剧本更新成功');
    } else {
      await createScenario(scenarioForm.value);
      ElMessage.success('剧本添加成功');
    }
    drawerVisible.value = false;
    loadScenarios();
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败');
  }
};

const deleteScenario = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除剧本"${row.title}"吗？`, '删除确认', { type: 'warning' });
    await deleteScenario(row.id);
    ElMessage.success('删除成功');
    loadScenarios();
  } catch { /* cancelled */ }
};

onMounted(loadScenarios);
</script>

<style scoped>
.admin-scenarios-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.header-actions { display: flex; gap: 8px; }
</style>
