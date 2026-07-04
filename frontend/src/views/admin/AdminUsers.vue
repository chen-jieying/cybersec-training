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

      <el-table :data="users" style="width: 100%;" border stripe v-loading="loading">
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
            <el-button size="small" type="danger" link @click="deleteRow(row)">删除</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh } from '@element-plus/icons-vue';
import { getAdminUsers, createAdminUser, updateAdminUser, deleteAdminUser } from '../../api';

const loading = ref(false);
const users = ref<any[]>([]);
const showAddTeacher = ref(false);
const showEditUser = ref(false);

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

const loadUsers = async () => {
  loading.value = true;
  try {
    const res = await getAdminUsers();
    users.value = res.data || [];
  } catch (e) {
    console.error('加载用户列表失败', e);
    ElMessage.error('加载用户列表失败');
  } finally {
    loading.value = false;
  }
};

const roleTagType = (role: string) => {
  const map: Record<string, any> = { admin: 'danger', teacher: 'warning', student: 'success' };
  return map[role] || 'info';
};

const roleText = (role: string) => {
  const map: Record<string, string> = { admin: '管理员', teacher: '教师', student: '学生' };
  return map[role] || role;
};

const confirmAddTeacher = async () => {
  if (!newTeacher.value.username || !newTeacher.value.fullName) {
    ElMessage.warning('请填写必填信息');
    return;
  }
  try {
    await createAdminUser({
      username: newTeacher.value.username,
      fullName: newTeacher.value.fullName,
      password: newTeacher.value.password,
      role: 'teacher',
      title: newTeacher.value.title,
      phone: newTeacher.value.phone,
    });
    ElMessage.success('教师添加成功');
    showAddTeacher.value = false;
    newTeacher.value = { username: '', fullName: '', password: '123456', title: '', phone: '' };
    loadUsers();
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '添加失败');
  }
};

const editUser = (row: any) => {
  editUserForm.value = {
    id: row.id,
    username: row.username,
    fullName: row.fullName,
    role: row.role,
    phone: row.phone || '',
  };
  showEditUser.value = true;
};

const confirmEditUser = async () => {
  try {
    await updateAdminUser(editUserForm.value.id, {
      fullName: editUserForm.value.fullName,
      phone: editUserForm.value.phone,
    });
    ElMessage.success('用户信息更新成功');
    showEditUser.value = false;
    loadUsers();
  } catch (e) {
    ElMessage.error('更新失败');
  }
};

const deleteRow = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.fullName}"吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    });
    await deleteAdminUser(row.id);
    ElMessage.success('删除成功');
    loadUsers();
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
</style>
