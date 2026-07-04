<template>
  <div class="student-stages-page">
    <el-card shadow="hover">
      <div class="page-header">
        <h3>闯关题库</h3>
        <p>完成每个关卡的安全知识问答，解锁更高级别的挑战</p>
      </div>

      <el-row :gutter="20" v-loading="loading">
        <el-col v-for="stage in stages" :key="stage.id" :span="8">
          <el-card shadow="hover" class="stage-card"
            :class="{ locked: !stage.unlocked }" @click="enterStage(stage)">
            <div class="stage-level">第{{ stage.id }}关</div>
            <el-icon :size="48" :color="stage.unlocked ? '#409EFF' : '#c0c4cc'"><Trophy /></el-icon>
            <h4 class="stage-name">{{ stage.name }}</h4>
            <p class="stage-desc">{{ stage.description }}</p>
            <div class="stage-stars" v-if="stage.stars > 0">
              <el-icon v-for="i in 3" :key="i" :color="i <= stage.stars ? '#F7BA2A' : '#c0c4cc'" :size="20"><StarFilled /></el-icon>
            </div>
            <div class="stage-info">
              <span>{{ stage.questionCount }}题</span>
              <span v-if="stage.bestScore > 0">最佳 {{ stage.bestScore }}/{{ stage.totalScore }}</span>
            </div>
            <el-tag v-if="!stage.unlocked" type="info" class="lock-tag"><el-icon><Lock /></el-icon> 未解锁</el-tag>
            <el-tag v-else-if="stage.bestScore === 0" type="primary" class="lock-tag">进入挑战</el-tag>
            <el-tag v-else type="success" class="lock-tag">再次挑战</el-tag>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="!loading && stages.length === 0" description="暂无可用关卡，请联系管理员添加题目" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Trophy, StarFilled, Lock } from '@element-plus/icons-vue';
import { getStudentStages } from '../../api';

const router = useRouter();
const loading = ref(false);
const stages = ref<any[]>([]);

const loadStages = async () => {
  loading.value = true;
  try {
    const res = await getStudentStages();
    stages.value = res.data || [];
  } catch {
    ElMessage.warning('加载关卡失败，使用缓存数据');
  } finally {
    loading.value = false;
  }
};

const enterStage = (stage: any) => {
  if (!stage.unlocked) {
    ElMessage.warning('该关卡尚未解锁，请先完成前置关卡');
    return;
  }
  if (stage.questionCount === 0) {
    ElMessage.warning('该关卡暂无题目');
    return;
  }
  router.push(`/student/exam/${stage.id}`);
};

onMounted(loadStages);
</script>

<style scoped>
.student-stages-page { padding: 0; }
.page-header { margin-bottom: 24px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.stage-card { text-align: center; padding: 20px; cursor: pointer; transition: transform 0.2s; height: 100%; }
.stage-card:not(.locked):hover { transform: translateY(-4px); box-shadow: 0 4px 16px rgba(64,158,255,0.2); }
.stage-card.locked { cursor: not-allowed; opacity: 0.7; filter: grayscale(30%); }
.stage-level { font-size: 14px; color: #909399; margin-bottom: 12px; }
.stage-name { margin: 12px 0 8px 0; font-size: 16px; }
.stage-desc { font-size: 13px; color: #909399; margin-bottom: 8px; min-height: 36px; }
.stage-stars { display: flex; justify-content: center; gap: 4px; margin-bottom: 8px; }
.stage-info { font-size: 12px; color: #909399; display: flex; justify-content: center; gap: 12px; margin-bottom: 8px; }
.lock-tag { margin-top: 8px; }
</style>
