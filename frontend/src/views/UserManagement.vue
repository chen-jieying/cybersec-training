<template>
  <div class="user-page">
    <el-card shadow="hover" class="user-card">
      <div class="header-row">
        <div>
          <h3>学生信息管理</h3>
          <p>教师和管理员可批量导入、导出学生列表，方便管理学员信息。</p>
        </div>
        <div>
          <el-upload
            class="upload-demo"
            action=""
            :on-change="handleFileChange"
            :auto-upload="false"
            :show-file-list="false"
          >
            <el-button size="small" type="primary">上传 Excel</el-button>
          </el-upload>
          <el-button size="small" @click="downloadExcel">导出 Excel</el-button>
        </div>
      </div>
      <el-table :data="users" style="width: 100%;">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="fullName" label="姓名" />
        <el-table-column prop="grade" label="年级" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { fetchUsers, importUsers, exportUsers } from '../api';
import { ElMessage } from 'element-plus';

const users = ref([]);

const loadUsers = async () => {
  const res = await fetchUsers();
  users.value = res.data;
};

const handleFileChange = async (file: File) => {
  try {
    await importUsers(file);
    ElMessage.success('Excel 导入成功');
    loadUsers();
  } catch (error) {
    ElMessage.error('导入失败，请检查文件格式');
  }
};

const downloadExcel = async () => {
  const res = await exportUsers();
  const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'students.xlsx';
  link.click();
  window.URL.revokeObjectURL(url);
};

loadUsers();
</script>

<style scoped>
.user-page {
  padding: 20px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.user-card {
  padding: 20px;
}
</style>
