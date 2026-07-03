<template>
  <div class="teacher-students-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>{{ className }} - 学生管理</h3>
          <p>管理该班级的学生信息</p>
        </div>
        <div class="header-actions">
          <el-upload
            action=""
            :on-change="handleImport"
            :auto-upload="false"
            :show-file-list="false"
            accept=".xlsx,.xls"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon> 导入 Excel
            </el-button>
          </el-upload>
          <el-button @click="exportExcel">
            <el-icon><Download /></el-icon> 导出 Excel
          </el-button>
          <el-button type="success" @click="showAddStudent = true">
            <el-icon><Plus /></el-icon> 新增学生
          </el-button>
        </div>
      </div>

      <el-table :data="students" border stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="fullName" label="姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="grade" label="年级" width="80" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="score" label="成绩" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.score >= 80 ? 'success' : row.score >= 60 ? 'warning' : 'danger'">
              {{ row.score || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="editStudent(row)">编辑</el-button>
            <el-button size="small" type="warning" link @click="resetPassword(row)">重置密码</el-button>
            <el-button size="small" type="danger" link @click="deleteStudent(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑学生弹窗 -->
    <el-dialog v-model="showAddStudent" :title="editingStudentId ? '编辑学生信息' : '新增学生'" width="500px" :close-on-click-modal="false">
      <el-form :model="studentForm" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="studentForm.fullName" />
        </el-form-item>
        <el-form-item label="学号" required>
          <el-input v-model="studentForm.studentNo" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="studentForm.username" :disabled="!!editingStudentId" />
        </el-form-item>
        <el-form-item v-if="!editingStudentId" label="初始密码">
          <el-input v-model="studentForm.password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddStudent = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveStudent">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Upload, Download, Plus } from '@element-plus/icons-vue';
import { mockStudents, mockClasses, Student } from '../../mock/data';

const route = useRoute();
const classId = computed(() => Number(route.params.classId));
const className = computed(() => {
  const cls = mockClasses.find(c => c.id === classId.value);
  return cls ? cls.className : '未知班级';
});

const students = ref<Student[]>([]);
const showAddStudent = ref(false);
const editingStudentId = ref<number | null>(null);

const studentForm = ref({
  fullName: '',
  studentNo: '',
  username: '',
  password: '123456',
});

const loadStudents = () => {
  students.value = mockStudents.filter(s => s.classId === classId.value);
};

const editStudent = (row: Student) => {
  editingStudentId.value = row.id;
  studentForm.value = {
    fullName: row.fullName,
    studentNo: row.studentNo,
    username: row.username,
    password: '',
  };
  showAddStudent.value = true;
};

const confirmSaveStudent = () => {
  if (!studentForm.value.fullName || !studentForm.value.studentNo) {
    ElMessage.warning('请填写必填信息');
    return;
  }
  if (editingStudentId.value) {
    const idx = students.value.findIndex(s => s.id === editingStudentId.value);
    if (idx >= 0) {
      students.value[idx].fullName = studentForm.value.fullName;
      students.value[idx].studentNo = studentForm.value.studentNo;
    }
    ElMessage.success('学生信息更新成功');
  } else {
    const newStudent: Student = {
      id: Date.now(),
      username: studentForm.value.username || `stu${Date.now()}`,
      fullName: studentForm.value.fullName,
      studentNo: studentForm.value.studentNo,
      grade: '初二',
      classId: classId.value,
      className: className.value,
    };
    students.value.push(newStudent);
    ElMessage.success('学生添加成功');
  }
  showAddStudent.value = false;
  editingStudentId.value = null;
  studentForm.value = { fullName: '', studentNo: '', username: '', password: '123456' };
};

const resetPassword = async (row: Student) => {
  try {
    await ElMessageBox.confirm(`确定要重置"${row.fullName}"的密码吗？密码将重置为123456`, '重置密码', { type: 'warning' });
    ElMessage.success(`已重置${row.fullName}的密码为123456`);
  } catch { /* cancelled */ }
};

const deleteStudent = async (row: Student) => {
  try {
    await ElMessageBox.confirm(`确定要删除学生"${row.fullName}"吗？`, '删除确认', { type: 'warning' });
    students.value = students.value.filter(s => s.id !== row.id);
    ElMessage.success('删除成功');
  } catch { /* cancelled */ }
};

const handleImport = async (file: any) => {
  ElMessage.success('Excel 导入成功（模拟）');
};

const exportExcel = () => {
  ElMessage.success('Excel 导出成功（模拟）');
};

onMounted(loadStudents);
</script>

<style scoped>
.teacher-students-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.header-actions { display: flex; gap: 8px; flex-wrap: wrap; }
</style>
