<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="login-left">
        <div class="platform-intro">
          <div class="platform-icon">
            <el-icon :size="64"><Monitor /></el-icon>
          </div>
          <h1>网络信息安全素养实训平台</h1>
          <p>提升网络安全意识，掌握安全防护技能</p>
          <div class="feature-list">
            <div class="feature-item"><el-icon><CircleCheck /></el-icon> AI情景仿真实训</div>
            <div class="feature-item"><el-icon><CircleCheck /></el-icon> 闯关答题挑战</div>
            <div class="feature-item"><el-icon><CircleCheck /></el-icon> 学情数据分析</div>
            <div class="feature-item"><el-icon><CircleCheck /></el-icon> 教学资源管理</div>
          </div>
        </div>
      </div>
      <div class="login-right">
        <el-card class="login-card" shadow="hover">
          <h2>用户登录</h2>
          <p class="login-subtitle">请选择角色并输入账号密码</p>
          <el-form :model="form" :rules="rules" ref="loginForm" label-width="80px" size="large">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input type="password" v-model="form.password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" placeholder="请选择角色" style="width:100%">
                <el-option label="学生" value="student" />
                <el-option label="教师" value="teacher" />
                <el-option label="管理员" value="admin" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>
          <div class="login-tips">
            <p>测试账号：管理员 admin1 / 123456 | 教师 teacher1 / 123456 | 学生 student1 / 123456</p>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Monitor, CircleCheck } from '@element-plus/icons-vue';
import { loginApi } from '../api';

const router = useRouter();
const loading = ref(false);

const form = ref({ username: '', password: '', role: '' });
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
};
const loginForm = ref();

const handleLogin = async () => {
  const valid = await loginForm.value.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    const res = await loginApi({
      username: form.value.username,
      password: form.value.password,
      role: form.value.role
    });
    const user = res.data;

    localStorage.setItem('currentRole', user.role);
    localStorage.setItem('currentUser', user.username);
    localStorage.setItem('currentFullName', user.fullName || user.username);
    localStorage.setItem('currentUserId', user.id?.toString() || '');
    const expiry = Date.now() + 24 * 60 * 60 * 1000;
    localStorage.setItem('tokenExpiry', expiry.toString());

    ElMessage.success(`欢迎回来，${user.fullName || user.username}！`);
    router.push('/dashboard');
  } catch (err: any) {
    const msg = err?.response?.data?.message || '登录失败，请检查用户名、密码或角色';
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page { height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #1a2a3a 0%, #2d4a6b 50%, #1a2a3a 100%); }
.login-bg { display: flex; width: 960px; min-height: 540px; border-radius: 16px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
.login-left { flex: 1; background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%); display: flex; align-items: center; justify-content: center; padding: 40px; color: #fff; }
.platform-intro { text-align: center; }
.platform-icon { margin-bottom: 20px; }
.platform-intro h1 { font-size: 24px; margin: 0 0 12px 0; font-weight: 700; }
.platform-intro p { font-size: 14px; opacity: 0.9; margin-bottom: 30px; }
.feature-list { text-align: left; display: inline-block; }
.feature-item { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; font-size: 14px; opacity: 0.95; }
.login-right { flex: 1; background: #fff; display: flex; align-items: center; justify-content: center; padding: 40px; }
.login-card { width: 100%; max-width: 380px; box-shadow: none; border: none; }
.login-card h2 { text-align: center; margin: 0 0 4px 0; color: #303133; }
.login-subtitle { text-align: center; color: #909399; font-size: 14px; margin: 0 0 30px 0; }
.login-tips { margin-top: 20px; padding: 12px; background: #f5f7fa; border-radius: 8px; font-size: 12px; color: #909399; text-align: center; }
.login-tips p { margin: 2px 0; }
</style>
