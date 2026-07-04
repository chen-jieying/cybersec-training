<template>
  <div class="teacher-chat-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>实训对话记录</h3>
          <p>查看本班全体学生的情景对话实训记录</p>
        </div>
        <div>
          <el-select v-model="selectedClassId" placeholder="选择班级" style="width:200px" @change="loadClassStudents">
            <el-option v-for="cls in classList" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
        </div>
      </div>

      <el-table :data="classStudents" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="fullName" label="学生姓名" width="120" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column label="完成场景数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.completedScenarios }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="平均得分" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.avgScore >= 80 ? 'success' : row.avgScore >= 60 ? 'warning' : 'danger'">
              {{ row.avgScore?.toFixed(1) || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="对话记录" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag size="small" style="margin:2px" type="info">
              聊天次数: {{ row.chatCount }}
            </el-tag>
            <span v-if="row.chatCount === 0" style="color:#909399">暂无记录</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewStudentChat(row)">
              <el-icon><View /></el-icon> 查看对话
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 学生对话详情弹窗 -->
    <el-dialog
      v-model="showChatDialog"
      :title="`${selectedStudentName} - 实训对话详情`"
      width="80%"
      top="5vh"
      :close-on-click-modal="false"
    >
      <div v-loading="detailLoading">
        <!-- 训练记录 -->
        <h4 style="margin-bottom:12px;">训练记录</h4>
        <el-table :data="selectedTrainingRecords" border stripe v-if="selectedTrainingRecords.length > 0">
          <el-table-column prop="scenarioId" label="场景ID" width="80" />
          <el-table-column prop="score" label="得分" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.score >= 80 ? 'success' : row.score >= 60 ? 'warning' : 'danger'">
                {{ row.score }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="得分率" min-width="200">
            <template #default="{ row }">
              <el-progress :percentage="Math.round(row.score / 100 * 100)"
                :color="row.score >= 80 ? '#67C23A' : row.score >= 60 ? '#E6A23C' : '#F56C6C'" />
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'completed' ? 'success' : 'info'">
                {{ row.status === 'completed' ? '已完成' : '进行中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="completedAt" label="完成时间" width="160" />
        </el-table>
        <el-empty v-else description="暂无训练记录" />

        <!-- 聊天记录 -->
        <h4 style="margin: 16px 0 12px;">聊天行为记录</h4>
        <el-table :data="selectedChatRecords" border stripe v-if="selectedChatRecords.length > 0">
          <el-table-column prop="actionType" label="行为类型" width="100" />
          <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
          <el-table-column prop="score" label="得分" width="80" align="center" />
          <el-table-column prop="createdAt" label="时间" width="160" />
        </el-table>
        <el-empty v-else description="暂无聊天记录" />
      </div>

      <template #footer>
        <el-button @click="showChatDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { View } from '@element-plus/icons-vue';
import { getTeacherClasses, getClassChatSummary, getStudentChatRecords } from '../../api';

const loading = ref(false);
const selectedClassId = ref<number | null>(null);
const showChatDialog = ref(false);
const detailLoading = ref(false);
const selectedStudentName = ref('');
const selectedStudentId = ref<number | null>(null);

const classList = ref<any[]>([]);
const classStudents = ref<any[]>([]);
const selectedTrainingRecords = ref<any[]>([]);
const selectedChatRecords = ref<any[]>([]);

// 加载教师班级列表
const loadClasses = async () => {
  try {
    const res = await getTeacherClasses();
    classList.value = res.data || [];
    if (classList.value.length > 0) {
      selectedClassId.value = classList.value[0].id;
      loadClassStudents();
    }
  } catch (e) {
    console.error('加载班级列表失败', e);
  }
};

// 加载班级学生汇总
const loadClassStudents = async () => {
  if (!selectedClassId.value) return;
  loading.value = true;
  try {
    const res = await getClassChatSummary(selectedClassId.value);
    classStudents.value = res.data || [];
  } catch (e) {
    console.error('加载班级汇总失败', e);
  } finally {
    loading.value = false;
  }
};

// 查看学生对话详情
const viewStudentChat = async (student: any) => {
  selectedStudentName.value = student.studentName || student.fullName || '';
  selectedStudentId.value = student.studentId;
  showChatDialog.value = true;
  detailLoading.value = true;
  try {
    const res = await getStudentChatRecords(student.studentId);
    const data = res.data || {};
    selectedTrainingRecords.value = data.trainingRecords || [];
    selectedChatRecords.value = data.chatRecords || [];
  } catch (e) {
    console.error('加载对话详情失败', e);
  } finally {
    detailLoading.value = false;
  }
};

onMounted(loadClasses);
</script>

<style scoped>
.teacher-chat-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
</style>
