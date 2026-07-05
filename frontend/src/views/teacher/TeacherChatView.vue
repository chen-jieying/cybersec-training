<template>
  <div class="teacher-chat-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-bar">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item>
          <span style="cursor:pointer;color:#409EFF;" @click="goBackToClasses">对话实训</span>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="selectedClass">
          <span style="cursor:pointer;color:#409EFF;" @click="goBackToTasks">{{ selectedClass.className }}</span>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="selectedTask">
          <span style="cursor:pointer;color:#409EFF;" @click="goBackToTaskDetail">{{ selectedTask.title }}</span>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="selectedStudent">
          {{ selectedStudent.fullName || selectedStudent.studentName }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 第一层：班级卡片列表 -->
    <template v-if="!selectedClass">
      <el-card shadow="hover">
        <div class="page-header">
          <div>
            <h3>对话实训</h3>
            <p>选择班级查看该班级的实训任务和学生对话情况</p>
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
              <el-tag type="primary" size="small">点击查看实训任务</el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!classesLoading && classList.length === 0" description="您还没有负责的班级" />
    </template>

    <!-- 第二层：选中班级的实训任务列表 -->
    <template v-if="selectedClass && !selectedTask">
      <el-card shadow="hover" style="margin-bottom:16px;">
        <div class="page-header">
          <div>
            <h3>{{ selectedClass.className }} - 实训任务</h3>
            <p>查看和管理该班级的实训任务，点击任务查看学生完成情况</p>
          </div>
          <el-button type="primary" @click="showCreateTask = true">
            <el-icon><Plus /></el-icon> 发布任务
          </el-button>
        </div>
      </el-card>

      <el-row :gutter="20" v-loading="tasksLoading">
        <el-col v-for="task in taskList" :key="task.id" :span="8" style="margin-bottom:16px;">
          <el-card shadow="hover" class="task-card" @click="selectTask(task)">
            <div class="task-card-header">
              <el-tag type="success" size="small">{{ task.completedCount }}/{{ task.studentCount }} 完成</el-tag>
              <span class="task-date">{{ task.createdAt }}</span>
            </div>
            <h4 class="task-title">{{ task.title }}</h4>
            <p class="task-desc">{{ task.description }}</p>
            <div class="task-footer">
              <el-tag type="primary" size="small">点击查看详情</el-tag>
              <el-button type="danger" size="small" link @click.stop="handleDeleteTask(task)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!tasksLoading && taskList.length === 0" description="该班级暂无实训任务，请先发布任务" />
    </template>

    <!-- 第三层：任务详情（词云图 + 学生列表） -->
    <template v-if="selectedTask && !selectedStudent">
      <el-card shadow="hover" style="margin-bottom:16px;">
        <div class="page-header">
          <h3>{{ selectedTask.title }}</h3>
          <p>{{ selectedTask.description }}</p>
        </div>
      </el-card>

      <!-- 词云图区域 -->
      <el-card shadow="hover" style="margin-bottom:16px;" v-loading="wordCloudLoading">
        <h4 style="margin:0 0 8px 0;">学生高频词分析（实时更新）</h4>
        <p style="color:#909399;font-size:13px;margin-bottom:16px;">展示该班级学生在对话实训中使用最多的词语</p>
        <div ref="wordCloudRef" style="width:100%;height:400px;"></div>
        <el-empty v-if="!wordCloudLoading && wordCloudData.length === 0" description="暂无词云数据" />
      </el-card>

      <!-- 学生完成情况列表 -->
      <el-card shadow="hover">
        <h4 style="margin:0 0 12px 0;">学生完成情况</h4>
        <el-table :data="taskStudents" border stripe v-loading="studentsLoading">
          <el-table-column prop="studentName" label="学生姓名" width="120" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column label="完成状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.completed ? 'success' : 'info'">
                {{ row.completed ? '已完成' : '未开始' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="得分" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.score >= 80 ? 'success' : row.score >= 60 ? 'warning' : row.score > 0 ? 'danger' : 'info'">
                {{ row.score }}/{{ row.maxScore }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="对话次数" width="100" align="center">
            <template #default="{ row }">
              <span :style="{ color: row.chatCount > 0 ? '#409EFF' : '#909399' }">{{ row.chatCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'completed' ? 'success' : 'info'" size="small">
                {{ row.status === 'completed' ? '已完成' : row.status === '未开始' ? '未开始' : '进行中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="completedAt" label="完成时间" width="160" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" :disabled="!row.completed" @click="viewStudentResponses(row)">
                <el-icon><View /></el-icon> 查看回答
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!studentsLoading && taskStudents.length === 0" description="该班级暂无学生" />
      </el-card>
    </template>

    <!-- 第四层：学生回答详情 -->
    <template v-if="selectedStudent">
      <el-card shadow="hover" style="margin-bottom:16px;">
        <div class="page-header">
          <div>
            <h3>{{ selectedStudent.studentName || selectedStudent.fullName }} - 实训回答详情</h3>
            <p>任务：{{ selectedTask?.title }} | 共 {{ studentResponses.length }} 条对话记录</p>
          </div>
        </div>
      </el-card>

      <!-- 训练记录 -->
      <el-card shadow="hover" style="margin-bottom:16px;" v-if="studentRecords.length > 0">
        <h4 style="margin:0 0 12px 0;">训练记录</h4>
        <el-table :data="studentRecords" border stripe>
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
      </el-card>

      <!-- 对话消息 -->
      <el-card shadow="hover" v-loading="responsesLoading">
        <h4 style="margin:0 0 16px 0;">对话消息记录（共{{ studentResponses.length }}条）</h4>
        <div v-if="studentResponses.length === 0" style="text-align:center;padding:40px;color:#909399;">
          暂无对话消息记录
        </div>
        <div v-else class="chat-message-list">
          <div
            v-for="(msg, idx) in studentResponses"
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
      </el-card>
    </template>

    <!-- 发布任务弹窗 -->
    <el-dialog v-model="showCreateTask" title="发布实训任务" width="500px" :close-on-click-modal="false">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务标题" required>
          <el-input v-model="taskForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="情景剧本" required>
          <el-select v-model="taskForm.scenarioId" placeholder="请选择情景剧本" style="width:100%">
            <el-option v-for="s in scenarios" :key="s.id" :label="s.title" :value="s.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTask = false">取消</el-button>
        <el-button type="primary" @click="confirmCreateTask" :loading="creating">确定发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { View, School, Plus } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import 'echarts-wordcloud';
import {
  getTeacherClasses, getClassTasks, createTask, deleteTask,
  getTaskWordCloud, getTaskStudents, getStudentTaskResponses,
  getTeacherScenarios
} from '../../api';

const classesLoading = ref(false);
const tasksLoading = ref(false);
const studentsLoading = ref(false);
const wordCloudLoading = ref(false);
const responsesLoading = ref(false);
const creating = ref(false);

const classList = ref<any[]>([]);
const selectedClass = ref<any>(null);
const taskList = ref<any[]>([]);
const selectedTask = ref<any>(null);
const taskStudents = ref<any[]>([]);
const wordCloudData = ref<any[]>([]);

const selectedStudent = ref<any>(null);
const studentResponses = ref<any[]>([]);
const studentRecords = ref<any[]>([]);

const showCreateTask = ref(false);
const taskForm = ref({ title: '', description: '', scenarioId: null as number | null, classId: 0 });
const scenarios = ref<any[]>([]);

const wordCloudRef = ref<HTMLElement | null>(null);
let wordCloudChart: any = null;
let autoRefreshTimer: any = null;

// 加载班级
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

// 选择班级，加载任务
const selectClass = async (cls: any) => {
  selectedClass.value = cls;
  tasksLoading.value = true;
  try {
    const res = await getClassTasks(cls.id);
    taskList.value = res.data || [];
  } catch (e) {
    console.error('加载任务失败', e);
  } finally {
    tasksLoading.value = false;
  }
};

// 选择任务，加载词云和学生
const selectTask = async (task: any) => {
  selectedTask.value = task;
  await Promise.all([loadWordCloud(task.id), loadTaskStudents(task.id)]);
  startAutoRefresh(task.id);
};

// 加载词云
const loadWordCloud = async (taskId: number) => {
  wordCloudLoading.value = true;
  try {
    const res = await getTaskWordCloud(taskId);
    const data = res.data || {};
    wordCloudData.value = data.wordCloud || [];
    await nextTick();
    renderWordCloud();
  } catch (e) {
    console.error('加载词云失败', e);
    wordCloudData.value = [];
  } finally {
    wordCloudLoading.value = false;
  }
};

// 加载任务学生
const loadTaskStudents = async (taskId: number) => {
  studentsLoading.value = true;
  try {
    const res = await getTaskStudents(taskId);
    taskStudents.value = res.data || [];
  } catch (e) {
    console.error('加载学生列表失败', e);
    taskStudents.value = [];
  } finally {
    studentsLoading.value = false;
  }
};

// 渲染词云
const renderWordCloud = () => {
  if (!wordCloudRef.value) return;
  if (wordCloudChart) wordCloudChart.dispose();
  if (wordCloudData.value.length === 0) return;

  wordCloudChart = echarts.init(wordCloudRef.value);
  wordCloudChart.setOption({
    tooltip: { show: true },
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      left: 'center',
      top: 'center',
      width: '90%',
      height: '90%',
      sizeRange: [14, 60],
      rotationRange: [-45, 45],
      rotationStep: 15,
      gridSize: 12,
      drawOutOfBound: false,
      textStyle: {
        fontFamily: 'sans-serif',
        fontWeight: 'normal',
        color: () => {
          const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#5470c6', '#91cc75', '#ee6666'];
          return colors[Math.floor(Math.random() * colors.length)];
        }
      },
      emphasis: {
        textStyle: { fontSize: 32, fontWeight: 'bold' }
      },
      data: wordCloudData.value.map((item: any) => ({
        name: item.name,
        value: item.value
      }))
    }]
  });
};

// 自动刷新词云（每30秒）
const startAutoRefresh = (taskId: number) => {
  stopAutoRefresh();
  autoRefreshTimer = setInterval(async () => {
    try {
      const res = await getTaskWordCloud(taskId);
      const data = res.data || {};
      wordCloudData.value = data.wordCloud || [];
      await nextTick();
      renderWordCloud();
    } catch { /* 静默失败 */ }
  }, 30000);
};

const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer);
    autoRefreshTimer = null;
  }
};

// 查看学生回答
const viewStudentResponses = async (student: any) => {
  selectedStudent.value = student;
  responsesLoading.value = true;
  try {
    const res = await getStudentTaskResponses(selectedTask.value.id, student.studentId);
    const data = res.data || {};
    studentResponses.value = data.messages || [];
    studentRecords.value = data.trainingRecords || [];
  } catch (e) {
    console.error('加载学生回答失败', e);
    studentResponses.value = [];
    studentRecords.value = [];
  } finally {
    responsesLoading.value = false;
  }
};

// 发布任务
const confirmCreateTask = async () => {
  if (!taskForm.value.title || !taskForm.value.scenarioId) {
    ElMessage.warning('请填写必填信息');
    return;
  }
  creating.value = true;
  try {
    await createTask({
      classId: selectedClass.value.id,
      scenarioId: taskForm.value.scenarioId,
      title: taskForm.value.title,
      description: taskForm.value.description
    });
    ElMessage.success('任务发布成功');
    showCreateTask.value = false;
    taskForm.value = { title: '', description: '', scenarioId: null, classId: 0 };
    await selectClass(selectedClass.value);
  } catch {
    ElMessage.error('发布失败');
  } finally {
    creating.value = false;
  }
};

// 删除任务
const handleDeleteTask = async (task: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除任务"${task.title}"吗？`, '确认删除', { type: 'warning' });
    await deleteTask(task.id);
    ElMessage.success('删除成功');
    await selectClass(selectedClass.value);
  } catch { /* 取消 */ }
};

// 加载情景剧本
const loadScenarios = async () => {
  try {
    const res = await getTeacherScenarios();
    scenarios.value = res.data || [];
  } catch { /* */ }
};

// 导航
const goBackToClasses = () => {
  stopAutoRefresh();
  selectedClass.value = null;
  selectedTask.value = null;
  selectedStudent.value = null;
  taskList.value = [];
  taskStudents.value = [];
  wordCloudData.value = [];
  studentResponses.value = [];
  studentRecords.value = [];
};

const goBackToTasks = () => {
  stopAutoRefresh();
  selectedTask.value = null;
  selectedStudent.value = null;
  taskStudents.value = [];
  wordCloudData.value = [];
  studentResponses.value = [];
  studentRecords.value = [];
};

const goBackToTaskDetail = () => {
  selectedStudent.value = null;
  studentResponses.value = [];
  studentRecords.value = [];
};

onMounted(() => {
  loadClasses();
  loadScenarios();
});
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

/* 任务卡片 */
.task-card { cursor: pointer; transition: all 0.25s; }
.task-card:hover { transform: translateY(-4px); box-shadow: 0 4px 20px rgba(103,194,58,0.15); }
.task-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.task-date { font-size: 12px; color: #909399; }
.task-title { margin: 0 0 8px 0; font-size: 16px; color: #303133; }
.task-desc { font-size: 13px; color: #606266; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.task-footer { display: flex; justify-content: space-between; align-items: center; }

/* 对话消息 */
.chat-message-list { max-height: 600px; overflow-y: auto; }
.chat-message-item {
  margin-bottom: 12px; padding: 12px 16px;
  border-radius: 8px; border-left: 4px solid #409EFF;
  background: #f0f7ff;
}
.chat-message-item.is-ai { border-left-color: #E6A23C; background: #fef8ed; }
.chat-message-item.is-user { border-left-color: #409EFF; background: #f0f7ff; }
.msg-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.msg-time { font-size: 12px; color: #909399; }
.msg-body { font-size: 14px; color: #303133; line-height: 1.7; white-space: pre-wrap; word-break: break-word; }
</style>
