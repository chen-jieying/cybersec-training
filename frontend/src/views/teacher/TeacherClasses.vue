<template>
  <div class="teacher-classes-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>班级管理</h3>
          <p>管理您所负责的班级信息（仅显示您名下的班级）</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="showAddClass = true"><el-icon><Plus /></el-icon> 新增班级</el-button>
        </div>
      </div>

      <el-table :data="classes" border stripe style="width:100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="className" label="班级名称" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="goStudents(row)">{{ row.className }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="teacherName" label="班主任" width="120" />
        <el-table-column prop="studentCount" label="学生人数" width="100" align="center" />
        <el-table-column label="班级均分" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getAvgScoreType(row)">{{ getAvgScore(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="goStudents(row)">查看学生</el-button>
            <el-button size="small" type="danger" link @click="deleteClass(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && classes.length === 0" description="您还没有负责的班级，请先添加班级" />
    </el-card>

    <!-- 新增班级弹窗 -->
    <el-dialog v-model="showAddClass" title="新增班级" width="500px" :close-on-click-modal="false">
      <el-form :model="classForm" label-width="100px">
        <el-form-item label="班级名称" required><el-input v-model="classForm.className" placeholder="如：初二(4)班" /></el-form-item>
        <el-form-item label="年级" required>
          <el-select v-model="classForm.grade" placeholder="请选择年级">
            <el-option label="初一" value="初一" /><el-option label="初二" value="初二" /><el-option label="初三" value="初三" />
          </el-select>
        </el-form-item>
        <el-form-item label="班主任"><el-input v-model="classForm.teacherName" placeholder="请输入班主任姓名" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddClass = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveClass" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getTeacherClasses, createClass, deleteClass, getClassStats } from '../../api';

const router = useRouter();
const classes = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);
const showAddClass = ref(false);
const classStats = ref<Record<number, any>>({});

const classForm = ref({ className: '', grade: '', teacherName: '' });

const loadClasses = async () => {
  loading.value = true;
  try {
    const res = await getTeacherClasses();
    classes.value = res.data || [];

    // 加载每个班级的均分
    for (const cls of classes.value) {
      try {
        const statsRes = await getClassStats(cls.id);
        classStats.value[cls.id] = statsRes.data;
      } catch { /* stats not available */ }
    }
  } catch {
    classes.value = [];
  } finally {
    loading.value = false;
  }
};

const getAvgScore = (row: any) => {
  const stats = classStats.value[row.id];
  return stats?.avgScore ?? 0;
};

const getAvgScoreType = (row: any) => {
  const score = getAvgScore(row);
  if (score >= 80) return 'success';
  if (score >= 60) return 'warning';
  if (score === 0) return 'info';
  return 'danger';
};

const goStudents = (row: any) => {
  router.push(`/teacher/classes/${row.id}/students`);
};

const confirmSaveClass = async () => {
  if (!classForm.value.className || !classForm.value.grade) {
    ElMessage.warning('请填写必填信息');
    return;
  }
  saving.value = true;
  try {
    await createClass({
      className: classForm.value.className,
      grade: classForm.value.grade,
      teacherName: classForm.value.teacherName || localStorage.getItem('currentFullName') || '教师',
      teacherId: Number(localStorage.getItem('currentUserId') || '2'),
    });
    ElMessage.success('班级添加成功');
    showAddClass.value = false;
    classForm.value = { className: '', grade: '', teacherName: '' };
    loadClasses();
  } catch {
    ElMessage.error('添加失败，请确认已连接后端服务');
  } finally {
    saving.value = false;
  }
};

const deleteClassHandler = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除班级"${row.className}"吗？`, '删除确认', { type: 'warning' });
    await deleteClass(row.id);
    ElMessage.success('删除成功');
    loadClasses();
  } catch { /* cancelled */ }
};

onMounted(loadClasses);
</script>

<style scoped>
.teacher-classes-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.header-actions { display: flex; gap: 8px; }
</style>
