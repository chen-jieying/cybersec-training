<template>
  <el-container style="height: 100vh;">
    <el-header height="60px" class="app-header">
      <div class="header-left">
        <el-button v-if="showBack" type="text" class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <span class="logo">网络信息安全素养实训平台</span>
      </div>
      <div class="header-actions">
        <el-dropdown trigger="click" v-if="currentUser">
          <span class="user-dropdown">
            <el-icon><UserFilled /></el-icon>
            {{ currentUser }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                <span style="color:#909399">{{ roleText }}</span>
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <!-- 侧边导航栏 -->
      <el-aside v-if="showSidebar" :width="sidebarCollapsed ? '64px' : '200px'" class="app-sidebar">
        <div class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :router="true"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <!-- Admin menu -->
          <template v-if="currentRole === 'admin'">
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/questions">
              <el-icon><Edit /></el-icon>
              <span>题库管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/scenarios">
              <el-icon><Document /></el-icon>
              <span>情景剧本管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/resources">
              <el-icon><FolderOpened /></el-icon>
              <span>资源管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/settings">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </template>

          <!-- Teacher menu -->
          <template v-if="currentRole === 'teacher'">
            <el-menu-item index="/teacher/classes">
              <el-icon><School /></el-icon>
              <span>班级管理</span>
            </el-menu-item>
            <el-menu-item index="/teacher/analytics">
              <el-icon><DataAnalysis /></el-icon>
              <span>学情数据分析</span>
            </el-menu-item>
            <el-menu-item index="/simulation">
              <el-icon><ChatDotRound /></el-icon>
              <span>对话实训</span>
            </el-menu-item>
            <el-menu-item index="/teacher/chat">
              <el-icon><ChatLineSquare /></el-icon>
              <span>学生实训记录</span>
            </el-menu-item>
            <el-menu-item index="/users">
              <el-icon><List /></el-icon>
              <span>学生信息管理</span>
            </el-menu-item>
            <el-menu-item index="/teacher/resources">
              <el-icon><FolderOpened /></el-icon>
              <span>教学资源</span>
            </el-menu-item>
          </template>

          <!-- Student menu -->
          <template v-if="currentRole === 'student'">
            <el-menu-item index="/student/stages">
              <el-icon><Trophy /></el-icon>
              <span>闯关题库</span>
            </el-menu-item>
            <el-menu-item index="/student/scenarios">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI情景仿真实训</span>
            </el-menu-item>
            <el-menu-item index="/student/profile">
              <el-icon><UserFilled /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <el-menu-item index="/student/resources">
              <el-icon><FolderOpened /></el-icon>
              <span>教学资源</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      <el-main :class="{ 'no-sidebar': !showSidebar }">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import {
  ArrowLeft, ArrowDown, UserFilled, SwitchButton, Fold, Expand,
  HomeFilled, User, Edit, Document, FolderOpened, Setting,
  School, DataAnalysis, ChatDotRound, ChatLineSquare, List, Trophy
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const currentUser = ref<string | null>(localStorage.getItem('currentUser'));
const currentRole = ref<string | null>(localStorage.getItem('currentRole'));
const sidebarCollapsed = ref(false);

const showSidebar = computed(() => {
  return route.path !== '/login' && route.path !== '/403';
});

const showBack = computed(() => {
  return route.path !== '/login' && route.path !== '/dashboard' && route.path !== '/403';
});

const activeMenu = computed(() => {
  const path = route.path;
  if (path.startsWith('/admin/')) return path;
  if (path.startsWith('/teacher/classes/')) return '/teacher/classes';
  if (path.startsWith('/teacher/chat')) return '/teacher/chat';
  if (path.startsWith('/teacher/')) return path;
  if (path.startsWith('/student/stages')) return '/student/stages';
  if (path.startsWith('/student/exam')) return '/student/stages';
  if (path.startsWith('/student/result')) return '/student/stages';
  if (path.startsWith('/student/chat')) return '/student/scenarios';
  if (path.startsWith('/student/')) return path;
  return path;
});

const roleText = computed(() => {
  const map: Record<string, string> = { admin: '管理员', teacher: '教师', student: '学生' };
  return map[currentRole.value || ''] || '';
});

const goBack = () => {
  router.back();
};

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });
    localStorage.removeItem('currentUser');
    localStorage.removeItem('currentRole');
    localStorage.removeItem('tokenExpiry');
    ElMessage.success('已退出登录');
    router.push('/login');
  } catch {
    // cancelled
  }
};

// Watch route changes to update user info
watch(() => route.path, () => {
  currentUser.value = localStorage.getItem('currentUser');
  currentRole.value = localStorage.getItem('currentRole');
});
</script>

<style scoped>
.app-header {
  background: linear-gradient(135deg, #1a2a3a 0%, #1f2d3d 100%);
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  z-index: 10;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.back-btn {
  color: #bfcbd9;
  font-size: 14px;
}
.back-btn:hover {
  color: #409EFF;
}
.logo {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
}
.header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-dropdown {
  color: #cfe3ff;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: background 0.2s;
}
.user-dropdown:hover {
  background: rgba(255,255,255,0.1);
}
.app-sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
  position: relative;
}
.sidebar-toggle {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bfcbd9;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.sidebar-toggle:hover {
  background: rgba(255,255,255,0.05);
}
.sidebar-menu {
  border-right: none;
}
:deep(.el-menu) {
  border-right: none;
}
:deep(.el-main) {
  background: #f0f2f5;
  padding: 20px;
  min-height: calc(100vh - 60px);
}
.no-sidebar {
  /* full width when no sidebar */
}
</style>
