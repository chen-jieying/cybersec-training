<template>
  <div class="teacher-analytics-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>学情数据分析</h3>
          <p>查看您名下所有班级的学情数据统计</p>
        </div>
        <div>
          <el-select v-model="selectedClassId" placeholder="选择班级" style="width:200px" @change="loadStats">
            <el-option v-for="cls in classes" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
        </div>
      </div>

      <div v-loading="loading">
        <!-- 数据汇总 -->
        <el-row :gutter="16" style="margin-bottom:20px;">
          <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ stats.avgScore }}</div><div class="summary-label">班级平均分</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ stats.passRate }}%</div><div class="summary-label">及格率</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ stats.completionRate }}%</div><div class="summary-label">实训完成率</div></el-card></el-col>
          <el-col :span="6"><el-card shadow="hover" class="summary-card"><div class="summary-value">{{ stats.totalExamCount }}</div><div class="summary-label">总答题次数</div></el-card></el-col>
        </el-row>

        <!-- 柱状图 -->
        <el-card shadow="hover">
          <div ref="barChartRef" style="width:100%;height:400px;"></div>
        </el-card>

        <el-empty v-if="!loading && (!selectedClassId)" description="请先选择班级查看数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import * as echarts from 'echarts';
import { getTeacherClasses, getClassStats, getClassStudents } from '../../api';

const loading = ref(false);
const classes = ref<any[]>([]);
const selectedClassId = ref<number | null>(null);
const barChartRef = ref<HTMLElement | null>(null);

const stats = ref({ avgScore: 0, passRate: 0, completionRate: 0, totalExamCount: 0 });
let chartInstance: any = null;

const loadClasses = async () => {
  try {
    const res = await getTeacherClasses();
    classes.value = res.data || [];
    if (classes.value.length > 0) {
      selectedClassId.value = classes.value[0].id;
      loadStats();
    }
  } catch {
    classes.value = [];
  }
};

const loadStats = async () => {
  if (!selectedClassId.value) return;
  loading.value = true;
  try {
    const [statsRes, studentsRes] = await Promise.all([
      getClassStats(selectedClassId.value),
      getClassStudents(selectedClassId.value)
    ]);
    stats.value = statsRes.data || { avgScore: 0, passRate: 0, completionRate: 0, totalExamCount: 0 };

    // 更新柱状图（按学生显示得分）
    const students = studentsRes.data || [];
    await nextTick();
    initBarChart(students);
  } catch {
    stats.value = { avgScore: 0, passRate: 0, completionRate: 0, totalExamCount: 0 };
  } finally {
    loading.value = false;
  }
};

const initBarChart = (students: any[]) => {
  if (!barChartRef.value) return;
  if (chartInstance) chartInstance.dispose();
  chartInstance = echarts.init(barChartRef.value);

  const names = students.map((s: any) => s.fullName || s.username || '');
  const scores = students.map((s: any) => s.avgScore || 0);

  chartInstance.setOption({
    title: { text: '学生平均成绩', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: names.length > 8 ? 45 : 0 } },
    yAxis: { type: 'value', max: 100, name: '分数' },
    series: [{
      name: '平均分', type: 'bar', data: scores,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' }, { offset: 1, color: '#79bbff' }
        ]),
        borderRadius: [6, 6, 0, 0]
      },
      barWidth: '50%', label: { show: true, position: 'top' }
    }]
  });
};

onMounted(loadClasses);
</script>

<style scoped>
.teacher-analytics-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.summary-card { text-align: center; padding: 16px 0; }
.summary-value { font-size: 36px; font-weight: 700; color: #409EFF; }
.summary-label { font-size: 14px; color: #909399; margin-top: 8px; }
</style>
