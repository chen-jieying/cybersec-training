<template>
  <div class="teacher-chat-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-bar">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item>
          <span style="cursor:pointer;color:#409EFF;" @click="goBackToClasses">对话实训</span>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="selectedClass">
          <span style="cursor:pointer;color:#409EFF;" @click="goBackToStudentList">{{ selectedClass.className }}</span>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="selectedStudent">
          {{ selectedStudent.studentName || selectedStudent.fullName }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 第一层：班级卡片列表 -->
    <template v-if="!selectedClass">
      <el-card shadow="hover">
        <div class="page-header">
          <div>
            <h3>对话实训记录</h3>
            <p>选择班级查看学生的情景对话实训情况</p>
          </div>
        </div>
      </el-card>

      <el-row :gutter="20" style="margin-top:16px;" v-loading="classesLoading">
        <el-col v-for="cls in classList" :key="cls.id" :span="8" style="margin-bottom:16px;">
          <el-card shadow="hover" class="class-card" @click="selectClass(cls)">
            <div class="class-card-content">
              <el-icon :size="40" color="#409EFF"><School /></el-icon>
              <h4>{{ cls.className }}</h4>
              <p>{{ cls.grade }} | {{ cls.teacherName }} | {{ cls.studentCount || 0 }}名学生</p>
              <el-tag type="primary" size="small">点击查看实训记录</el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!classesLoading && classList.length === 0" description="您还没有负责的班级" />
    </template>

    <!-- 第二层：选中班级的学生列表 -->
    <template v-if="selectedClass && !selectedStudent">
      <el-card shadow="hover" style="margin-bottom:16px;">
        <div class="page-header">
          <div>
            <h3>{{ selectedClass.className }} - 学生实训记录</h3>
            <p>点击"查看对话"浏览每个学生的详细实训内容</p>
          </div>
        </div>
      </el-card>

      <el-table :data="classStudents" border stripe style="width:100%" v-loading="studentsLoading">
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="className" label="年级" width="100" />
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
        <el-table-column label="对话次数" width="120" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.chatCount > 0 ? '#409EFF' : '#909399' }">{{ row.chatCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewStudentDetailedChat(row)">
              <el-icon><View /></el-icon> 查看对话
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!studentsLoading && classStudents.length === 0" description="该班级暂无学生实训记录" />
    </template>

    <!-- 第三层：选中学生的对话详情 -->
    <template v-if="selectedStudent">
      <el-card shadow="hover" style="margin-bottom:16px;">
        <div class="page-header">
          <div>
            <h3>{{ selectedStudent.studentName || selectedStudent.fullName }} - 实训对话详情</h3>
            <p>共 {{ chatMessages.length }} 条对话消息，{{ trainingRecords.length }} 条训练记录</p>
          </div>
        </div>
      </el-card>

      <!-- 训练记录概览 -->
      <el-card shadow="hover" style="margin-bottom:16px;">
        <h4 style="margin:0 0 12px 0;">训练记录</h4>
        <el-table :data="trainingRecords" border stripe v-if="trainingRecords.length > 0">
          <el-table-column prop="scenarioId" label="场景ID" width="80" />
          <el-table-column prop="scenarioTitle" label="场景名称" min-width="180" />
          <el-table-column label="得分" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.score >= 80 ? 'success' : row.score >= 60 ? 'warning' : 'danger'">
                {{ row.score }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="得分率" min-width="200">
            <template #default="{ row }">
              <el-progress :percentage="row.maxScore > 0 ? Math.round(row.score / row.maxScore * 100) : 0"
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
      </el-card>

      <!-- 对话消息详情（每条语句独立展示） -->
      <el-card shadow="hover">
        <h4 style="margin:0 0 16px 0;">对话消息记录（共{{ chatMessages.length }}条）</h4>
        <div v-loading="messagesLoading">
          <div v-if="chatMessages.length === 0" style="text-align:center;padding:40px;color:#909399;">
            暂无对话消息记录
          </div>
          <div v-else class="chat-message-list">
            <div
              v-for="(msg, idx) in chatMessages"
              :key="msg.id || idx"
              class="chat-message-item"
              :class="{ 'is-ai': msg.actionType === 'reply', 'is-user': msg.actionType === 'chat' || msg.actionType === 'message' }"
            >
              <div class="msg-header">
                <el-tag size="small" :type="msg.actionType === 'reply' ? 'warning' : 'primary'" effect="plain">
                  {{ msg.actionType === 'reply' ? 'AI回复' : '学生发言' }}
                </el-tag>
                <span class="msg-time">{{ msg.createdAt }}</span>
              </div>
              <div class="msg-body">{{ msg.detail || msg.description || '(无内容)' }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { View, School } from '@element-plus/icons-vue';
import { getTeacherClasses, getClassChatSummary, getStudentChatMessages, getStudentChatRecords } from '../../api';

const classesLoading = ref(false);
const studentsLoading = ref(false);
const messagesLoading = ref(false);

const classList = ref<any[]>([]);
const selectedClass = ref<any>(null);
const classStudents = ref<any[]>([]);

const selectedStudent = ref<any>(null);
const chatMessages = ref<any[]>([]);
const trainingRecords = ref<any[]>([]);

const loadClasses = async () => {
  classesLoading.value = true;
  try {
    const res = await getTeacherClasses();
    classList.value = res.data || [];
  } catch (e) {
    console.error('加载班级失败', e);
  } finally {
    classesLoading.value = false;
  }
};

const selectClass = async (cls: any) => {
  selectedClass.value = cls;
  studentsLoading.value = true;
  try {
    const res = await getClassChatSummary(cls.id);
    classStudents.value = res.data || [];
  } catch (e) {
    console.error('加载学生汇总失败', e);
  } finally {
    studentsLoading.value = false;
  }
};

const viewStudentDetailedChat = async (student: any) => {
  selectedStudent.value = student;
  messagesLoading.value = true;
  try {
    // 使用新的消息详情API获取每条对话消息
    const msgRes = await getStudentChatMessages(student.studentId);
    const msgData = msgRes.data || {};
    chatMessages.value = msgData.messages || [];
    trainingRecords.value = msgData.trainingRecords || [];

    // 如果消息为空，尝试旧的 API 获取训练记录
    if (chatMessages.value.length === 0 && trainingRecords.value.length === 0) {
      const oldRes = await getStudentChatRecords(student.studentId);
      const oldData = oldRes.data || {};
      trainingRecords.value = oldData.trainingRecords || [];
      chatMessages.value = oldData.chatRecords || [];
    }
  } catch (e) {
    console.error('加载对话详情失败', e);
    // 降级到旧API
    try {
      const res = await getStudentChatRecords(student.studentId);
      const data = res.data || {};
      trainingRecords.value = data.trainingRecords || [];
      chatMessages.value = data.chatRecords || [];
    } catch {
      chatMessages.value = [];
      trainingRecords.value = [];
    }
  } finally {
    messagesLoading.value = false;
  }
};

const goBackToClasses = () => {
  selectedClass.value = null;
  selectedStudent.value = null;
  classStudents.value = [];
  chatMessages.value = [];
  trainingRecords.value = [];
};

const goBackToStudentList = () => {
  selectedStudent.value = null;
  chatMessages.value = [];
  trainingRecords.value = [];
};

onMounted(loadClasses);
</script>

<style scoped>
.teacher-chat-page { padding: 0; }

.breadcrumb-bar {
  margin-bottom: 16px; padding: 8px 16px;
  background: #fff; border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }

/* 班级卡片 */
.class-card { cursor: pointer; transition: all 0.25s; }
.class-card:hover { transform: translateY(-4px); box-shadow: 0 4px 20px rgba(64,158,255,0.15); }
.class-card-content { text-align: center; padding: 20px 0; }
.class-card-content h4 { margin: 12px 0 8px 0; font-size: 18px; color: #303133; }
.class-card-content p { margin: 0 0 12px 0; color: #909399; font-size: 13px; }

/* 对话消息列表 */
.chat-message-list { max-height: 600px; overflow-y: auto; }
.chat-message-item {
  margin-bottom: 12px; padding: 12px 16px;
  border-radius: 8px; border-left: 4px solid #409EFF;
  background: #f0f7ff; transition: background 0.2s;
}
.chat-message-item.is-ai {
  border-left-color: #E6A23C; background: #fef8ed;
}
.chat-message-item.is-user {
  border-left-color: #409EFF; background: #f0f7ff;
}
.msg-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.msg-time { font-size: 12px; color: #909399; }
.msg-body { font-size: 14px; color: #303133; line-height: 1.7; white-space: pre-wrap; word-break: break-word; }
</style>
