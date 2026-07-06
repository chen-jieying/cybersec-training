<template>
  <div class="class-detail-page">
    <!-- 顶部标题栏 -->
    <div class="page-top-bar">
      <div class="top-left">
        <el-button type="default" link @click="$router.push('/teacher/classes')">
          <el-icon><ArrowLeft /></el-icon> 返回班级列表
        </el-button>
        <h3>{{ className }} - 班级管理</h3>
      </div>
      <div>
        <el-button v-if="activeMenu === 'students'" type="success" @click="showAddStudent = true">
          <el-icon><Plus /></el-icon> 新增学生
        </el-button>
      </div>
    </div>

    <!-- 统计卡片（始终显示） -->
    <el-row :gutter="16" style="margin-bottom:16px;" v-if="classStats">
      <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ classStats.totalStudents || 0 }}</div><div class="stat-label">学生总数</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ classStats.avgScore || 0 }}</div><div class="stat-label">班级均分</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ classStats.passRate || 0 }}%</div><div class="stat-label">及格率</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ classStats.completionRate || 0 }}%</div><div class="stat-label">完成率</div></el-card></el-col>
    </el-row>

    <!-- 主体布局：左侧导航 + 右侧内容 -->
    <div class="main-layout">
      <!-- 左侧导航栏 -->
      <div class="side-nav">
        <div
          class="nav-item"
          :class="{ active: activeMenu === 'students' }"
          @click="activeMenu = 'students'"
        >
          <el-icon><User /></el-icon>
          <span>学生管理</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: activeMenu === 'analytics' }"
          @click="switchToAnalytics"
        >
          <el-icon><DataAnalysis /></el-icon>
          <span>学情数据分析</span>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="content-area">
        <!-- 学生管理视图 -->
        <el-card v-if="activeMenu === 'students'" shadow="hover">
          <el-table :data="students" border stripe style="width:100%" v-loading="loading">
            <el-table-column prop="fullName" label="姓名" width="120">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewStudentDetail(row)">{{ row.fullName }}</el-button>
              </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="grade" label="年级" width="80" />
            <el-table-column label="平均分" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.avgScore >= 80 ? 'success' : row.avgScore >= 60 ? 'warning' : row.avgScore > 0 ? 'danger' : 'info'">
                  {{ row.avgScore }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="考试次数" width="100" align="center">
              <template #default="{ row }">{{ row.examCount || 0 }}</template>
            </el-table-column>
            <el-table-column label="正确率" min-width="150">
              <template #default="{ row }">
                <el-progress :percentage="row.totalQuestions > 0 ? Math.round(row.correctQuestions / row.totalQuestions * 100) : 0"
                  :color="row.totalQuestions > 0 ? (row.correctQuestions / row.totalQuestions >= 0.6 ? '#67C23A' : '#E6A23C') : '#909399'" />
              </template>
            </el-table-column>
            <el-table-column label="实训状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.trainingStatus === '待教师评分' ? 'warning' : row.trainingStatus === '已完成' ? 'success' : 'info'" size="small">
                  {{ row.trainingStatus || '未开始' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="资源学习" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.resourceCount > 0 ? 'success' : 'info'" size="small">
                  {{ row.resourceCount || 0 }}个
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="viewStudentDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && students.length === 0" description="该班级暂无学生" />
        </el-card>

        <!-- 学情数据分析视图 -->
        <div v-if="activeMenu === 'analytics'" v-loading="analyticsLoading">
          <!-- 数据分析汇总 -->
          <el-row :gutter="16" style="margin-bottom:20px;">
            <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ analyticsStats.avgScore }}</div><div class="summary-label">班级平均分</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ analyticsStats.passRate }}%</div><div class="summary-label">及格率</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ analyticsStats.completionRate }}%</div><div class="summary-label">实训完成率</div></el-card></el-col>
            <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ analyticsStats.totalExamCount }}</div><div class="summary-label">总答题次数</div></el-card></el-col>
          </el-row>

          <!-- 柱状图：学生平均成绩 -->
          <el-card shadow="hover" style="margin-bottom:20px;">
            <div ref="barChartRef" style="width:100%;height:400px;"></div>
            <el-empty v-if="analyticsStudents.length === 0" description="暂无学生数据" />
          </el-card>

          <!-- 各关卡成绩分布 -->
          <el-card shadow="hover" v-if="analyticsStudents.length > 0">
            <h4 style="margin:0 0 16px 0;">各关卡答题情况分析</h4>
            <el-table :data="stageAnalysis" border stripe>
              <el-table-column prop="stage" label="关卡" width="120" />
              <el-table-column prop="attemptCount" label="答题人次" width="120" align="center" />
              <el-table-column label="平均得分" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.avgScore >= 80 ? 'success' : row.avgScore >= 60 ? 'warning' : 'danger'">
                    {{ row.avgScore }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="correctCount" label="正确题数" width="100" align="center" />
              <el-table-column prop="totalCount" label="总题数" width="100" align="center" />
              <el-table-column label="正确率" min-width="200">
                <template #default="{ row }">
                  <el-progress :percentage="row.totalCount > 0 ? Math.round(row.correctCount / row.totalCount * 100) : 0"
                    :color="row.totalCount > 0 && row.correctCount / row.totalCount >= 0.6 ? '#67C23A' : '#E6A23C'" />
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 学生答题详情弹窗 -->
    <el-dialog v-model="showDetail" :title="`${detailStudent?.fullName || ''} - 学习详情`" width="85%" top="3vh" :close-on-click-modal="false">
      <div v-loading="detailLoading">
        <el-row :gutter="16" style="margin-bottom:16px;" v-if="examDetail">
          <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ examDetail.totalExams || 0 }}</div><div class="stat-label">考试次数</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#67C23A">{{ examDetail.correctQuestions || 0 }}</div><div class="stat-label">正确题数</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#409EFF">{{ detailTrainingRecords ? detailTrainingRecords.length : 0 }}</div><div class="stat-label">实训次数</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ examDetail.totalQuestions > 0 ? Math.round(examDetail.correctQuestions / examDetail.totalQuestions * 100) : 0 }}%</div><div class="stat-label">正确率</div></el-card></el-col>
        </el-row>
        <el-tabs v-model="detailActiveTab" type="border-card">
          <!-- 答题记录标签页 -->
          <el-tab-pane label="闯关答题记录" name="exam">
            <el-tabs v-model="activeSession" type="card" v-if="examDetail && examDetail.examSessions && examDetail.examSessions.length > 0">
              <el-tab-pane v-for="session in examDetail.examSessions" :key="session.sessionId"
                :label="`第${session.stageId || '?'}关 (${session.score}分)`" :name="session.sessionId">
                <el-table :data="session.questions" border stripe>
                  <el-table-column type="index" label="#" width="50" />
                  <el-table-column prop="questionText" label="题目" min-width="300" show-overflow-tooltip />
                  <el-table-column label="学生选择" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag :type="row.isCorrect ? 'success' : 'danger'">{{ row.userAnswer || '-' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="正确答案" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag type="success" v-if="!row.isCorrect">{{ row.correctAnswer }}</el-tag>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="结果" width="80" align="center">
                    <template #default="{ row }">
                      <el-tag :type="row.isCorrect ? 'success' : 'danger'" size="small">{{ row.isCorrect ? '正确' : '错误' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="得分" width="80" align="center" />
                  <el-table-column prop="answeredAt" label="答题时间" width="180" />
                </el-table>
                <div v-for="item in session.questions.filter((q: any) => !q.isCorrect && q.explanation)" :key="'exp-' + item.questionId"
                  class="exam-explanation" style="margin-top:8px; padding:12px; background:#f0f7ff; border-radius:6px;">
                  <strong style="color:#409EFF">题目{{ item.questionId }}解析：</strong>{{ item.explanation }}
                </div>
              </el-tab-pane>
            </el-tabs>
            <el-empty v-else description="该学生暂无答题记录" />
          </el-tab-pane>

          <!-- 情景实训记录标签页 -->
          <el-tab-pane label="情景实训记录" name="training">
            <el-table :data="detailTrainingRecords" border stripe v-if="detailTrainingRecords && detailTrainingRecords.length > 0">
              <el-table-column prop="scenarioTitle" label="实训场景" min-width="150" />
              <el-table-column label="状态" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === '待教师评分' ? 'warning' : 'success'" size="small">{{ row.status || '未评分' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="score" label="教师评分" width="100" align="center" />
              <el-table-column prop="maxScore" label="满分" width="80" align="center" />
              <el-table-column prop="completedAt" label="完成时间" width="180" />
            </el-table>
            <el-empty v-else description="该学生暂无实训记录" />

            <!-- 对话详情 -->
            <div v-if="detailChatMessages && detailChatMessages.length > 0" style="margin-top:16px;">
              <h4>对话记录</h4>
              <div class="chat-detail-list">
                <div v-for="(msg, idx) in detailChatMessages" :key="idx" class="chat-detail-item"
                  :class="{ fromStudent: msg.actionType === 'chat', fromBot: msg.actionType === 'reply' }">
                  <div class="chat-detail-role">{{ msg.actionType === 'chat' ? '学生' : msg.actionType === 'reply' ? 'AI教练' : '系统' }}</div>
                  <div class="chat-detail-content">{{ msg.detail || msg.content || '' }}</div>
                  <div class="chat-detail-time">{{ msg.createdAt }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 资源学习记录标签页 -->
          <el-tab-pane label="资源学习记录" name="resource">
            <el-table :data="detailResourceRecords" border stripe v-if="detailResourceRecords && detailResourceRecords.length > 0">
              <el-table-column prop="detail" label="学习行为" min-width="300" show-overflow-tooltip />
              <el-table-column prop="createdAt" label="时间" width="180" />
            </el-table>
            <el-empty v-else description="该学生暂无资源学习记录" />
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer><el-button @click="showDetail = false">关闭</el-button></template>
    </el-dialog>

    <!-- 新增学生弹窗 -->
    <el-dialog v-model="showAddStudent" title="新增学生" width="500px" :close-on-click-modal="false">
      <el-form :model="studentForm" label-width="100px">
        <el-form-item label="姓名" required><el-input v-model="studentForm.fullName" /></el-form-item>
        <el-form-item label="用户名" required><el-input v-model="studentForm.username" placeholder="登录账号" /></el-form-item>
        <el-form-item label="初始密码" required><el-input v-model="studentForm.password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddStudent = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveStudent" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Plus, User, DataAnalysis } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { getClassStudents, getClassStats, getStudentExamDetail, createAdminUser, getStudentChatRecords } from '../../api';
import api from '../../api';

const route = useRoute();
const classId = computed(() => Number(route.params.classId));

const activeMenu = ref('students');
const loading = ref(false);
const saving = ref(false);
const students = ref<any[]>([]);
const classStats = ref<any>(null);
const className = ref('');

const showDetail = ref(false);
const detailLoading = ref(false);
const detailActiveTab = ref('exam');
const activeSession = ref('');
const examDetail = ref<any>(null);
const detailStudent = ref<any>(null);
const detailTrainingRecords = ref<any[]>([]);
const detailChatMessages = ref<any[]>([]);
const detailResourceRecords = ref<any[]>([]);

const showAddStudent = ref(false);
const studentForm = ref({ fullName: '', username: '', password: '123456' });

// 学情分析相关
const analyticsLoading = ref(false);
const analyticsStudents = ref<any[]>([]);
const analyticsStats = ref({ avgScore: 0, passRate: 0, completionRate: 0, totalExamCount: 0 });
const stageAnalysis = ref<any[]>([]);
const barChartRef = ref<HTMLElement | null>(null);
let chartInstance: any = null;

const loadData = async () => {
  loading.value = true;
  try {
    const [studentsRes, statsRes] = await Promise.all([
      getClassStudents(classId.value),
      getClassStats(classId.value)
    ]);
    students.value = studentsRes.data || [];
    classStats.value = statsRes.data || { totalStudents: 0, avgScore: 0, passRate: 0, completionRate: 0 };

    // 批量获取每个学生的实训和资源状态
    const statusPromises = students.value.map(async (s: any) => {
      try {
        const [trainRes, resourceRes] = await Promise.all([
          api.get(`/teacher/chat-records/${s.id}`),
          api.get(`/teacher/resource-records/${s.id}`)
        ]);
        const trainingRecords = (trainRes.data?.trainingRecords || []);
        const resourceRecords = (resourceRes.data || []);
        s.trainingStatus = trainingRecords.length > 0
          ? (trainingRecords[trainingRecords.length - 1]?.status || '已完成')
          : '未开始';
        s.resourceCount = resourceRecords.length;
      } catch {
        s.trainingStatus = '未知';
        s.resourceCount = 0;
      }
    });
    await Promise.all(statusPromises);

    if (students.value.length > 0) {
      className.value = students.value[0].grade ? `${students.value[0].grade}班` : `班级${classId.value}`;
    } else {
      className.value = `班级${classId.value}`;
    }
  } catch {
    students.value = [];
    classStats.value = { totalStudents: 0, avgScore: 0, passRate: 0, completionRate: 0 };
  } finally {
    loading.value = false;
  }
};

const switchToAnalytics = async () => {
  activeMenu.value = 'analytics';
  analyticsLoading.value = true;
  try {
    const [statsRes, studentsRes] = await Promise.all([
      getClassStats(classId.value),
      getClassStudents(classId.value)
    ]);
    analyticsStats.value = statsRes.data || { avgScore: 0, passRate: 0, completionRate: 0, totalExamCount: 0 };
    analyticsStudents.value = studentsRes.data || [];

    // 计算各关卡分析
    computeStageAnalysis();

    await nextTick();
    initBarChart(analyticsStudents.value);
  } catch {
    analyticsStats.value = { avgScore: 0, passRate: 0, completionRate: 0, totalExamCount: 0 };
  } finally {
    analyticsLoading.value = false;
  }
};

const computeStageAnalysis = () => {
  // 基础的关卡分析 - 基于学生的答题数据
  stageAnalysis.value = [
    { stage: '基础知识', attemptCount: 0, avgScore: 0, correctCount: 0, totalCount: 0 },
    { stage: '钓鱼防护', attemptCount: 0, avgScore: 0, correctCount: 0, totalCount: 0 },
    { stage: '社交工程', attemptCount: 0, avgScore: 0, correctCount: 0, totalCount: 0 },
  ];
};

const initBarChart = (studentList: any[]) => {
  if (!barChartRef.value) return;
  if (chartInstance) chartInstance.dispose();
  chartInstance = echarts.init(barChartRef.value);

  const names = studentList.map((s: any) => s.fullName || s.username || '');
  const scores = studentList.map((s: any) => s.avgScore || 0);

  chartInstance.setOption({
    title: { text: '学生平均成绩对比', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: names.length > 8 ? 45 : 0 } },
    yAxis: { type: 'value', max: 100, name: '分数' },
    series: [{
      name: '平均分', type: 'bar', data: scores,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' }, { offset: 1, color: '#79bbff' }
        ]),
        borderRadius: [6, 6, 0, 0]
      },
      barWidth: '50%', label: { show: true, position: 'top' }
    }]
  });
};

const viewStudentDetail = async (student: any) => {
  detailStudent.value = student;
  showDetail.value = true;
  detailLoading.value = true;
  detailActiveTab.value = 'exam';
  examDetail.value = null;
  detailTrainingRecords.value = [];
  detailChatMessages.value = [];
  detailResourceRecords.value = [];

  try {
    const [examRes, chatRes, resourceRes] = await Promise.all([
      getStudentExamDetail(student.id),
      api.get(`/teacher/chat-records/${student.id}`),
      api.get(`/teacher/resource-records/${student.id}`)
    ]);

    examDetail.value = examRes.data || { totalExams: 0, totalQuestions: 0, correctQuestions: 0, examSessions: [] };
    if (examDetail.value?.examSessions?.length > 0) {
      activeSession.value = examDetail.value.examSessions[0].sessionId;
    }

    detailTrainingRecords.value = (chatRes.data?.trainingRecords || []);

    // 获取对话消息
    try {
      const msgRes = await getStudentChatRecords(student.id);
      detailChatMessages.value = (msgRes.data?.chatRecords || []).slice(0, 50);
    } catch {
      detailChatMessages.value = [];
    }

    detailResourceRecords.value = (resourceRes.data || []).filter((r: any) =>
      r.actionType === 'resource_view' || r.actionType === 'resource_download'
    );
  } catch {
    examDetail.value = { totalExams: 0, totalQuestions: 0, correctQuestions: 0, examSessions: [] };
  } finally {
    detailLoading.value = false;
  }
};

const confirmSaveStudent = async () => {
  if (!studentForm.value.fullName || !studentForm.value.username) {
    ElMessage.warning('请填写必填信息');
    return;
  }
  saving.value = true;
  try {
    await createAdminUser({
      username: studentForm.value.username,
      password: studentForm.value.password,
      fullName: studentForm.value.fullName,
      role: 'student',
      grade: '初二',
      classId: classId.value
    });
    ElMessage.success('学生添加成功');
    showAddStudent.value = false;
    studentForm.value = { fullName: '', username: '', password: '123456' };
    loadData();
  } catch {
    ElMessage.error('添加失败');
  } finally {
    saving.value = false;
  }
};

onMounted(loadData);
</script>

<style scoped>
.class-detail-page { padding: 0; min-height: 600px; }
.page-top-bar {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; padding: 12px 16px;
  background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.top-left { display: flex; align-items: center; gap: 16px; }
.top-left h3 { margin: 0; font-size: 18px; }

.main-layout { display: flex; gap: 16px; }
.side-nav {
  width: 200px; flex-shrink: 0;
  background: #fff; border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  padding: 8px 0; height: fit-content;
}
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 20px; cursor: pointer;
  color: #606266; font-size: 14px;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}
.nav-item:hover { background: #f5f7fa; color: #409EFF; }
.nav-item.active {
  background: #ecf5ff; color: #409EFF;
  border-left-color: #409EFF; font-weight: 600;
}

.content-area { flex: 1; min-width: 0; }

.stat-card { text-align: center; padding: 12px 0; }
.stat-value { font-size: 28px; font-weight: 700; color: #409EFF; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

.summary-card { text-align: center; padding: 16px 0; }
.summary-value { font-size: 36px; font-weight: 700; color: #409EFF; }
.summary-label { font-size: 14px; color: #909399; margin-top: 8px; }

.exam-explanation { margin-bottom: 8px; font-size: 13px; line-height: 1.8; }

.chat-detail-list { max-height: 400px; overflow-y: auto; }
.chat-detail-item { padding: 10px 14px; margin-bottom: 8px; border-radius: 8px; background: #f5f7fa; }
.chat-detail-item.fromStudent { background: #ecf5ff; border-left: 3px solid #409EFF; }
.chat-detail-item.fromBot { background: #f0f9eb; border-left: 3px solid #67C23A; }
.chat-detail-role { font-size: 12px; color: #909399; margin-bottom: 4px; }
.chat-detail-content { font-size: 13px; line-height: 1.7; word-break: break-word; }
.chat-detail-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; text-align: right; }
</style>
