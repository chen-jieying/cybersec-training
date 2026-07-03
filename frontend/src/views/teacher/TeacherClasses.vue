<template>
  <div class="teacher-classes-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>班级管理</h3>
          <p>管理您所负责的班级信息</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="showAddClass = true">
            <el-icon><Plus /></el-icon> 新增班级
          </el-button>
        </div>
      </div>

      <el-table :data="classes" border stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="className" label="班级名称" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="goStudents(row)">{{ row.className }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="teacherName" label="班主任" width="120" />
        <el-table-column prop="studentCount" label="学生人数" width="100" align="center" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="editClass(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="deleteClass(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增班级弹窗 -->
    <el-dialog v-model="showAddClass" :title="editingClassId ? '编辑班级' : '新增班级'" width="500px" :close-on-click-modal="false">
      <el-form :model="classForm" label-width="100px">
        <el-form-item label="班级名称" required>
          <el-input v-model="classForm.className" placeholder="如：初二(4)班" />
        </el-form-item>
        <el-form-item label="年级" required>
          <el-select v-model="classForm.grade" placeholder="请选择年级">
            <el-option label="初一" value="初一" />
            <el-option label="初二" value="初二" />
            <el-option label="初三" value="初三" />
          </el-select>
        </el-form-item>
        <el-form-item label="班主任">
          <el-input v-model="classForm.teacherName" placeholder="请输入班主任姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddClass = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveClass">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { mockClasses, SchoolClass } from '../../mock/data';

const router = useRouter();
const classes = ref<SchoolClass[]>([]);
const showAddClass = ref(false);
const editingClassId = ref<number | null>(null);

const classForm = ref({
  className: '',
  grade: '',
  teacherName: '',
});

const loadClasses = () => {
  classes.value = [...mockClasses];
};

const goStudents = (row: SchoolClass) => {
  router.push(`/teacher/classes/${row.id}/students`);
};

const editClass = (row: SchoolClass) => {
  editingClassId.value = row.id;
  classForm.value = { className: row.className, grade: row.grade, teacherName: row.teacherName };
  showAddClass.value = true;
};

const confirmSaveClass = () => {
  if (!classForm.value.className || !classForm.value.grade) {
    ElMessage.warning('请填写必填信息');
    return;
  }
  if (editingClassId.value) {
    const idx = classes.value.findIndex(c => c.id === editingClassId.value);
    if (idx >= 0) {
      classes.value[idx] = { ...classes.value[idx], ...classForm.value };
    }
    ElMessage.success('班级信息更新成功');
  } else {
    const newClass: SchoolClass = {
      id: Date.now(),
      grade: classForm.value.grade,
      className: classForm.value.className,
      teacherId: 2,
      teacherName: classForm.value.teacherName || '李老师',
      studentCount: 0,
    };
    classes.value.push(newClass);
    ElMessage.success('班级添加成功');
  }
  showAddClass.value = false;
  editingClassId.value = null;
  classForm.value = { className: '', grade: '', teacherName: '' };
};

const deleteClass = async (row: SchoolClass) => {
  try {
    await ElMessageBox.confirm(`确定要删除班级"${row.className}"吗？`, '删除确认', { type: 'warning' });
    classes.value = classes.value.filter(c => c.id !== row.id);
    ElMessage.success('删除成功');
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
