<template>
  <div class="dashboard-page">
    <!-- 管理员首页 -->
    <template v-if="currentRole === 'admin'">
      <el-row :gutter="20" style="margin-bottom:20px;">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="go('/admin/users')">
            <div class="stat-content">
              <el-icon :size="36" color="#409EFF"><User /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalStudents + stats.totalTeachers }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="go('/admin/questions')">
            <div class="stat-content">
              <el-icon :size="36" color="#67C23A"><Edit /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalQuestions }}</div>
                <div class="stat-label">题库数量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="go('/admin/scenarios')">
            <div class="stat-content">
              <el-icon :size="36" color="#E6A23C"><Document /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalScenarios }}</div>
                <div class="stat-label">情景剧本</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="go('/admin/resources')">
            <div class="stat-content">
              <el-icon :size="36" color="#F56C6C"><FolderOpened /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalResources }}</div>
                <div class="stat-label">教学资源</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-card shadow="hover" class="welcome-card">
        <h2>欢迎回来，管理员！</h2>
        <p>系统运行正常，请通过侧边栏菜单管理各项功能。</p>
      </el-card>
    </template>

    <!-- 教师首页 -->
    <template v-if="currentRole === 'teacher'">
      <el-row :gutter="20" style="margin-bottom:20px;">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="go('/teacher/classes')">
            <div class="stat-content">
              <el-icon :size="36" color="#409EFF"><School /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalClasses }}</div>
                <div class="stat-label">我的班级</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="go('/teacher/analytics')">
            <div class="stat-content">
              <el-icon :size="36" color="#67C23A"><DataAnalysis /></el-icon>
              <div>
                <div class="stat-number">{{ stats.avgScore }}</div>
                <div class="stat-label">班级均分</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="go('/teacher/chat')">
            <div class="stat-content">
              <el-icon :size="36" color="#E6A23C"><ChatDotRound /></el-icon>
              <div>
                <div class="stat-number">{{ stats.completionRate }}%</div>
                <div class="stat-label">对话实训记录</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card" @click="go('/teacher/resources')">
            <div class="stat-content">
              <el-icon :size="36" color="#F56C6C"><FolderOpened /></el-icon>
              <div>
                <div class="stat-number">{{ stats.totalResources }}</div>
                <div class="stat-label">教学资源</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-card shadow="hover" class="welcome-card">
        <h2>欢迎回来，老师！</h2>
        <p>请通过侧边栏管理班级、查看学情数据和学生对话实训记录。</p>
      </el-card>
    </template>

    <!-- 学生首页 -->
    <template v-if="currentRole === 'student'">
      <el-row :gutter="20" style="margin-bottom:20px;">
        <el-col :span="8">
          <el-card shadow="hover" class="entry-card" @click="go('/student/stages')">
            <div class="entry-content">
              <el-icon :size="48" color="#409EFF"><Trophy /></el-icon>
              <h3>闯关题库</h3>
              <p>完成安全知识闯关，提升防护能力</p>
              <el-tag type="primary">进入挑战</el-tag>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="entry-card" @click="go('/student/scenarios')">
            <div class="entry-content">
              <el-icon :size="48" color="#67C23A"><ChatDotRound /></el-icon>
              <h3>AI情景仿真实训</h3>
              <p>与AI进行多轮对话，模拟真实安全场景</p>
              <el-tag type="success">开始实训</el-tag>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="entry-card" @click="go('/student/profile')">
            <div class="entry-content">
              <el-icon :size="48" color="#E6A23C"><UserFilled /></el-icon>
              <h3>个人中心</h3>
              <p>查看成绩、错题记录和素养评估</p>
              <el-tag type="warning">查看详情</el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-card shadow="hover" class="welcome-card">
        <h2>欢迎回来，同学！</h2>
        <p>请选择上方功能卡片开始你的网络安全学习之旅。</p>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  User, Edit, Document, FolderOpened, School, DataAnalysis,
  Trophy, ChatDotRound, UserFilled
} from '@element-plus/icons-vue';
import { mockStats, mockClasses, mockQuestions, mockScenarios, mockResources } from '../mock/data';

const router = useRouter();
const currentRole = ref(localStorage.getItem('currentRole') || 'student');

const stats = computed(() => ({
  ...mockStats,
  totalClasses: mockClasses.length,
  totalQuestions: mockQuestions.length,
  totalScenarios: mockScenarios.length,
  totalResources: mockResources.length,
}));

const go = (path: string) => router.push(path);
</script>

<style scoped>
.dashboard-page {
  padding: 0;
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
.welcome-card {
  margin-top: 20px;
  text-align: center;
  padding: 40px;
}
.welcome-card h2 {
  margin: 0 0 8px 0;
  color: #303133;
}
.welcome-card p {
  color: #909399;
  margin: 0;
}
.entry-card {
  cursor: pointer;
  transition: transform 0.2s;
  height: 100%;
}
.entry-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(64,158,255,0.15);
}
.entry-content {
  text-align: center;
  padding: 20px 0;
}
.entry-content h3 {
  margin: 16px 0 8px 0;
}
.entry-content p {
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}
</style>
