<template>
  <div class="teacher-students-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>{{ className }} - 学生管理</h3>
          <p>查看班级学生列表及答题情况，点击学生姓名查看详细答题记录</p>
        </div>
        <div>
          <el-button type="success" @click="showAddStudent = true"><el-icon><Plus /></el-icon> 新增学生</el-button>
        </div>
      </div>

      <!-- 班级统计 -->
      <el-row :gutter="16" style="margin-bottom:16px;" v-if="classStats">
        <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ classStats.totalStudents || 0 }}</div><div class="stat-label">学生总数</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ classStats.avgScore || 0 }}</div><div class="stat-label">班级均分</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ classStats.passRate || 0 }}%</div><div class="stat-label">及格率</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ classStats.completionRate || 0 }}%</div><div class="stat-label">完成率</div></el-card></el-col>
      </el-row>

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
        <el-table-column label="正确率" min-width="200">
          <template #default="{ row }">
            <el-progress :percentage="row.totalQuestions > 0 ? Math.round(row.correctQuestions / row.totalQuestions * 100) : 0"
              :color="row.totalQuestions > 0 ? (row.correctQuestions / row.totalQuestions >= 0.6 ? '#67C23A' : '#E6A23C') : '#909399'" />
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

    <!-- 学生答题详情弹窗 -->
    <el-dialog v-model="showDetail" :title="`${detailStudent?.fullName || ''} - 答题详情`" width="80%" top="5vh" :close-on-click-modal="false">
      <div v-loading="detailLoading">
        <!-- 汇总统计 -->
        <el-row :gutter="16" style="margin-bottom:16px;" v-if="examDetail">
          <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ examDetail.totalExams || 0 }}</div><div class="stat-label">考试次数</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#67C23A">{{ examDetail.correctQuestions || 0 }}</div><div class="stat-label">正确题数</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value" style="color:#F56C6C">{{ (examDetail.totalQuestions || 0) - (examDetail.correctQuestions || 0) }}</div><div class="stat-label">错误题数</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="stat-card"><div class="stat-value">{{ examDetail.totalQuestions > 0 ? Math.round(examDetail.correctQuestions / examDetail.totalQuestions * 100) : 0 }}%</div><div class="stat-label">正确率</div></el-card></el-col>
        </el-row>

        <!-- 每次考试的答题详情 -->
        <el-tabs v-model="activeSession" type="border-card" v-if="examDetail && examDetail.examSessions">
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
            <!-- 解析展示 -->
            <div v-for="item in session.questions.filter((q: any) => !q.isCorrect && q.explanation)" :key="'exp-' + item.questionId"
              class="exam-explanation" style="margin-top:8px; padding:12px; background:#f0f7ff; border-radius:6px;">
              <strong style="color:#409EFF">题目{{ item.questionId }}解析：</strong>{{ item.explanation }}
            </div>
          </el-tab-pane>
        </el-tabs>

        <el-empty v-if="!detailLoading && (!examDetail || !examDetail.examSessions || examDetail.examSessions.length === 0)"
          description="该学生暂无答题记录" />
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
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getClassStudents, getClassStats, getStudentExamDetail, createAdminUser } from '../../api';

const route = useRoute();
const classId = computed(() => Number(route.params.classId));

const loading = ref(false);
const saving = ref(false);
const students = ref<any[]>([]);
const classStats = ref<any>(null);
const className = ref('');

const showDetail = ref(false);
const detailLoading = ref(false);
const activeSession = ref('');
const examDetail = ref<any>(null);
const detailStudent = ref<any>(null);

const showAddStudent = ref(false);
const studentForm = ref({ fullName: '', username: '', password: '123456' });

const loadData = async () => {
  loading.value = true;
  try {
    const [studentsRes, statsRes] = await Promise.all([
      getClassStudents(classId.value),
      getClassStats(classId.value)
    ]);
    students.value = studentsRes.data || [];
    classStats.value = statsRes.data || { totalStudents: 0, avgScore: 0, passRate: 0, completionRate: 0 };

    // 获取班级名称
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

const viewStudentDetail = async (student: any) => {
  detailStudent.value = student;
  showDetail.value = true;
  detailLoading.value = true;
  examDetail.value = null;
  try {
    const res = await getStudentExamDetail(student.id);
    examDetail.value = res.data;
    if (examDetail.value?.examSessions?.length > 0) {
      activeSession.value = examDetail.value.examSessions[0].sessionId;
    }
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
.teacher-students-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.stat-card { text-align: center; padding: 12px 0; }
.stat-value { font-size: 28px; font-weight: 700; color: #409EFF; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.exam-explanation { margin-bottom: 8px; font-size: 13px; line-height: 1.8; }
</style>
