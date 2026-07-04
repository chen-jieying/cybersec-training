<template>
  <div class="teacher-chat-page">
    <!-- 班级学生对话实训总览 -->
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>实训对话记录</h3>
          <p>查看本班全体学生的情景对话实训记录</p>
        </div>
        <div>
          <el-select v-model="selectedClassId" placeholder="选择班级" style="width:200px" @change="loadClassStudents">
            <el-option v-for="cls in mockClasses" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
        </div>
      </div>

      <!-- 学生列表 -->
      <el-table :data="classStudents" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="fullName" label="学生姓名" width="120" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column label="完成场景数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ getStudentChatCount(row.id) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="平均得分" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStudentAvgScore(row.id) >= 80 ? 'success' : getStudentAvgScore(row.id) >= 60 ? 'warning' : 'danger'">
              {{ getStudentAvgScore(row.id) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="对话记录" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag
              v-for="record in getStudentRecords(row.id)"
              :key="record.id"
              size="small"
              style="margin:2px"
              :type="record.status === 'completed' ? 'success' : 'info'"
            >
              {{ record.scenarioTitle }}
            </el-tag>
            <span v-if="getStudentRecords(row.id).length === 0" style="color:#909399">暂无记录</span>
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
      :title="`${selectedStudent?.fullName || ''} - 实训对话详情`"
      width="80%"
      top="5vh"
      :close-on-click-modal="false"
    >
      <el-tabs v-model="activeRecordTab" type="border-card">
        <!-- 对话记录 -->
        <el-tab-pane v-for="record in selectedStudentRecords" :key="record.id"
          :label="record.scenarioTitle" :name="String(record.id)">
          <div v-if="record.messages && record.messages.length > 0" class="chat-record-panel">
            <!-- 对话统计 -->
            <el-row :gutter="16" style="margin-bottom:16px;">
              <el-col :span="8">
                <el-statistic title="得分" :value="record.score" suffix="分" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="满分" :value="record.maxScore" suffix="分" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="完成时间" :value="record.completedAt" />
              </el-col>
            </el-row>

            <!-- 对话消息流 -->
            <div class="chat-messages">
              <div v-for="(msg, idx) in record.messages" :key="idx"
                :class="['chat-msg', msg.role === 'user' ? 'msg-user' : 'msg-bot']">
                <div class="msg-label">
                  {{ msg.role === 'user' ? selectedStudent?.fullName : '系统' }}
                </div>
                <div class="msg-bubble">{{ msg.text }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="该场景暂无对话记录" />
        </el-tab-pane>

        <!-- 训练记录汇总 -->
        <el-tab-pane label="训练记录汇总" name="summary">
          <el-table :data="selectedStudentRecords" border stripe>
            <el-table-column prop="scenarioTitle" label="场景名称" min-width="200" />
            <el-table-column prop="score" label="得分" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.score >= 80 ? 'success' : row.score >= 60 ? 'warning' : 'danger'">
                  {{ row.score }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="maxScore" label="满分" width="80" align="center" />
            <el-table-column label="得分率" min-width="200">
              <template #default="{ row }">
                <el-progress :percentage="Math.round(row.score / row.maxScore * 100)"
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
            <el-table-column prop="completedAt" label="完成时间" width="120" />
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="showChatDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { View } from '@element-plus/icons-vue';
import { mockClasses, mockStudents, mockStudentChatRecords, StudentChatRecord } from '../../mock/data';
import type { User } from '../../mock/data';

const loading = ref(false);
const selectedClassId = ref<number | null>(1);
const showChatDialog = ref(false);
const activeRecordTab = ref('summary');
const selectedStudent = ref<User | null>(null);

// 获取当前班级的学生列表（从 mock 数据中筛选）
const classStudents = computed(() => {
  if (!selectedClassId.value) return [];
  return mockStudents.filter(s => s.classId === selectedClassId.value);
});

// 获取某个学生对话记录数量
const getStudentChatCount = (studentId: number): number => {
  return mockStudentChatRecords.filter(r => r.studentId === studentId).length;
};

// 获取某个学生平均得分
const getStudentAvgScore = (studentId: number): number => {
  const records = mockStudentChatRecords.filter(r => r.studentId === studentId);
  if (records.length === 0) return 0;
  const sum = records.reduce((acc, r) => acc + r.score, 0);
  return Math.round(sum / records.length);
};

// 获取某个学生的实训记录
const getStudentRecords = (studentId: number): StudentChatRecord[] => {
  return mockStudentChatRecords.filter(r => r.studentId === studentId);
};

// 被选中学生的实训记录
const selectedStudentRecords = ref<StudentChatRecord[]>([]);

// 查看学生对话详情
const viewStudentChat = (student: any) => {
  selectedStudent.value = student;
  selectedStudentRecords.value = mockStudentChatRecords.filter(r => r.studentId === student.id);
  activeRecordTab.value = selectedStudentRecords.value.length > 0
    ? String(selectedStudentRecords.value[0].id) : 'summary';
  showChatDialog.value = true;
};

// 班级切换
const loadClassStudents = () => {
  loading.value = true;
  setTimeout(() => { loading.value = false; }, 300);
};
</script>

<style scoped>
.teacher-chat-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }

/* 对话消息样式 */
.chat-record-panel { padding: 8px; }
.chat-messages { max-height: 400px; overflow-y: auto; padding: 12px; background: #f5f7fa; border-radius: 8px; }
.chat-msg { margin-bottom: 12px; }
.chat-msg.msg-user { display: flex; flex-direction: column; align-items: flex-end; }
.chat-msg.msg-bot { display: flex; flex-direction: column; align-items: flex-start; }
.msg-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
.msg-bubble { max-width: 70%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.6; }
.msg-user .msg-bubble { background: #409EFF; color: #fff; border-bottom-right-radius: 4px; }
.msg-bot .msg-bubble { background: #fff; color: #303133; border: 1px solid #e4e7ed; border-bottom-left-radius: 4px; }
</style>
