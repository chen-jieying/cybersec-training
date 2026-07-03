<template>
  <div class="admin-settings-page">
    <el-card shadow="hover">
      <div class="page-header">
        <h3>系统设置</h3>
        <p>配置系统基本参数和运行环境</p>
      </div>

      <el-form label-width="140px" style="max-width:600px">
        <el-divider content-position="left">基本设置</el-divider>
        <el-form-item label="系统名称">
          <el-input v-model="settings.systemName" />
        </el-form-item>
        <el-form-item label="系统版本">
          <el-input v-model="settings.version" disabled />
        </el-form-item>
        <el-form-item label="Token有效期(小时)">
          <el-input-number v-model="settings.tokenExpiry" :min="1" :max="168" />
        </el-form-item>

        <el-divider content-position="left">安全设置</el-divider>
        <el-form-item label="登录失败锁定次数">
          <el-input-number v-model="settings.maxLoginAttempts" :min="3" :max="10" />
        </el-form-item>
        <el-form-item label="密码最小长度">
          <el-input-number v-model="settings.minPasswordLength" :min="6" :max="20" />
        </el-form-item>
        <el-form-item label="会话超时(分钟)">
          <el-input-number v-model="settings.sessionTimeout" :min="15" :max="480" />
        </el-form-item>

        <el-divider content-position="left">通知设置</el-divider>
        <el-form-item label="新用户注册通知">
          <el-switch v-model="settings.notifyNewUser" />
        </el-form-item>
        <el-form-item label="实训完成通知">
          <el-switch v-model="settings.notifyTraining" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
          <el-button @click="resetSettings">恢复默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';

const settings = reactive({
  systemName: '网络信息安全素养实训平台',
  version: 'v1.0.0',
  tokenExpiry: 24,
  maxLoginAttempts: 5,
  minPasswordLength: 6,
  sessionTimeout: 120,
  notifyNewUser: true,
  notifyTraining: true,
});

const saveSettings = () => {
  ElMessage.success('系统设置保存成功');
};

const resetSettings = () => {
  settings.systemName = '网络信息安全素养实训平台';
  settings.tokenExpiry = 24;
  settings.maxLoginAttempts = 5;
  settings.minPasswordLength = 6;
  settings.sessionTimeout = 120;
  settings.notifyNewUser = true;
  settings.notifyTraining = true;
  ElMessage.success('已恢复默认设置');
};
</script>

<style scoped>
.admin-settings-page { padding: 0; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; margin-bottom: 20px; }
</style>
