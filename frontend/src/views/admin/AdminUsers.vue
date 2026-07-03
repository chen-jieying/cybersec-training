<template>
  <div class="admin-users-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>用户管理</h3>
          <p>管理系统中的所有用户账号</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="showAddTeacher = true">
            <el-icon><Plus /></el-icon> 新增教师
          </el-button>
          <el-button @click="loadUsers">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="showStatDetail('students')">
            <div class="stat-content">
              <el-icon :size="32" color="#409EFF"><UserIcon /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalStudents }}</div>
                <div class="stat-label">学生总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="showStatDetail('teachers')">
            <div class="stat-content">
              <el-icon :size="32" color="#67C23A"><SchoolIcon /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalTeachers }}</div>
                <div class="stat-label">教师总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="showStatDetail('scenarios')">
            <div class="stat-content">
              <el-icon :size="32" color="#E6A23C"><DocIcon /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalScenarios }}</div>
                <div class="stat-label">情景剧本</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="showStatDetail('resources')">
            <div class="stat-content">
              <el-icon :size="32" color="#F56C6C"><FolderOpened /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalResources }}</div>
                <div class="stat-label">教学资源</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 用户表格 -->
      <el-table :data="users" style="width: 100%; margin-top: 16px;" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="fullName" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="title" label="职称" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="editUser(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增教师弹窗 -->
    <el-dialog v-model="showAddTeacher" title="新增教师" width="500px" :close-on-click-modal="false">
      <el-form :model="newTeacher" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="newTeacher.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="newTeacher.fullName" placeholder="请输入教师姓名" />
        </el-form-item>
        <el-form-item label="初始密码" required>
          <el-input v-model="newTeacher.password" placeholder="请输入初始密码" show-password />
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="newTeacher.title" placeholder="请选择职称">
            <el-option label="高级教师" value="高级教师" />
            <el-option label="中级教师" value="中级教师" />
            <el-option label="初级教师" value="初级教师" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="newTeacher.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddTeacher = false">取消</el-button>
        <el-button type="primary" @click="confirmAddTeacher">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="showEditUser" title="编辑用户" width="500px">
      <el-form :model="editUserForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="editUserForm.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editUserForm.fullName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editUserForm.role" disabled>
            <el-option label="管理员" value="admin" />
            <el-option label="教师" value="teacher" />
            <el-option label="学生" value="student" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editUserForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditUser = false">取消</el-button>
        <el-button type="primary" @click="confirmEditUser">保存</el-button>
      </template>
    </el-dialog>

    <!-- 统计详情弹窗 -->
    <el-dialog v-model="showStat" :title="statTitle" width="600px">
      <el-table :data="statDetailData" border stripe>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="count" label="数量" width="120" />
        <el-table-column prop="desc" label="说明" />
      </el-table>
      <template #footer>
        <el-button @click="showStat = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, User as UserIcon, School as SchoolIcon, Document as DocIcon, FolderOpened } from '@element-plus/icons-vue';
import { mockUsers, mockClasses, mockScenarios, mockResources, mockStats, type User } from '../../mock/data';

const users = ref<User[]>([]);
const stats = ref(mockStats);
const showAddTeacher = ref(false);
const showEditUser = ref(false);
const showStat = ref(false);
const statTitle = ref('');
const statDetailData = ref<any[]>([]);

const newTeacher = ref({
  username: '',
  fullName: '',
  password: '123456',
  title: '',
  phone: '',
});

const editUserForm = ref({
  id: 0,
  username: '',
  fullName: '',
  role: '',
  phone: '',
});

const loadUsers = () => {
  users.value = [...mockUsers];
};

const roleTagType = (role: string) => {
  const map: Record<string, any> = { admin: 'danger', teacher: 'warning', student: 'success' };
  return map[role] || 'info';
};

const roleText = (role: string) => {
  const map: Record<string, string> = { admin: '管理员', teacher: '教师', student: '学生' };
  return map[role] || role;
};

const showStatDetail = (type: string) => {
  showStat.value = true;
  if (type === 'students') {
    statTitle.value = '学生分类汇总';
    statDetailData.value = mockClasses.map(c => ({ name: c.className, count: c.studentCount, desc: `${c.grade}年级` }));
  } else if (type === 'teachers') {
    statTitle.value = '教师分类汇总';
    statDetailData.value = [
      { name: '高级教师', count: 3, desc: '具有高级职称的教师' },
      { name: '中级教师', count: 4, desc: '具有中级职称的教师' },
      { name: '初级教师', count: 1, desc: '具有初级职称的教师' },
    ];
  } else if (type === 'scenarios') {
    statTitle.value = '情景剧本汇总';
    statDetailData.value = mockScenarios.map(s => ({ name: s.title, count: 1, desc: s.difficulty === 'easy' ? '简单' : s.difficulty === 'medium' ? '中等' : '困难' }));
  } else {
    statTitle.value = '教学资源汇总';
    statDetailData.value = mockResources.map(r => ({ name: r.title, count: 1, desc: r.resourceType.toUpperCase() }));
  }
};

const confirmAddTeacher = () => {
  if (!newTeacher.value.username || !newTeacher.value.fullName) {
    ElMessage.warning('请填写必填信息');
    return;
  }
  const newUser: User = {
    id: users.value.length + 10,
    username: newTeacher.value.username,
    fullName: newTeacher.value.fullName,
    role: 'teacher',
    title: newTeacher.value.title,
    phone: newTeacher.value.phone,
  };
  users.value.push(newUser);
  stats.value.totalTeachers++;
  showAddTeacher.value = false;
  newTeacher.value = { username: '', fullName: '', password: '123456', title: '', phone: '' };
  ElMessage.success('教师添加成功');
};

const editUser = (row: User) => {
  editUserForm.value = { ...row, role: row.role || '' };
  showEditUser.value = true;
};

const confirmEditUser = () => {
  const idx = users.value.findIndex(u => u.id === editUserForm.value.id);
  if (idx >= 0) {
    users.value[idx].fullName = editUserForm.value.fullName;
    users.value[idx].phone = editUserForm.value.phone;
  }
  showEditUser.value = false;
  ElMessage.success('用户信息更新成功');
};

const deleteUser = async (row: User) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.fullName}"吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    });
    users.value = users.value.filter(u => u.id !== row.id);
    ElMessage.success('删除成功');
  } catch { /* cancelled */ }
};

onMounted(loadUsers);
</script>

<style scoped>
.admin-users-page {
  padding: 0;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h3 {
  margin: 0 0 4px 0;
}
.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.stats-row {
  margin-bottom: 8px;
}
.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
