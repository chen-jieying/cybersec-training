<template>
  <div class="teacher-analytics-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>学情数据分析</h3>
          <p>查看班级学生的学情数据统计和分析</p>
        </div>
      </div>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="雷达图" name="radar">
          <div class="chart-container">
            <echart-radar :data="radarData" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="柱状图" name="bar">
          <div class="chart-container">
            <div ref="barChartRef" style="width:100%;height:400px;"></div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 数据汇总 -->
      <el-row :gutter="16" style="margin-top: 20px;">
        <el-col :span="8">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value">{{ stats.avgScore }}</div>
            <div class="summary-label">班级平均分</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value">{{ stats.passRate }}%</div>
            <div class="summary-label">及格率</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="summary-card">
            <div class="summary-value">{{ stats.completionRate }}%</div>
            <div class="summary-label">实训完成率</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import EchartRadar from '../../components/EchartRadar.vue';

const activeTab = ref('radar');
const barChartRef = ref<HTMLElement | null>(null);

const radarData = ref({
  labels: ['安全意识', '钓鱼识别', '密码管理', '隐私保护', '应急响应'],
  scores: [78, 85, 72, 68, 74],
});

const stats = ref({
  avgScore: 78.5,
  passRate: 92.3,
  completionRate: 85.2,
});

const initBarChart = () => {
  if (!barChartRef.value) return;
  const chart = echarts.init(barChartRef.value);
  chart.setOption({
    title: { text: '各维度平均得分', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: ['安全意识', '钓鱼识别', '密码管理', '隐私保护', '应急响应'],
    },
    yAxis: { type: 'value', max: 100 },
    series: [{
      name: '平均分',
      type: 'bar',
      data: [78, 85, 72, 68, 74],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#79bbff' },
        ]),
        borderRadius: [6, 6, 0, 0],
      },
      barWidth: '40%',
      label: { show: true, position: 'top' },
    }],
  });
};

onMounted(() => {
  nextTick(() => {
    if (activeTab.value === 'bar') initBarChart();
  });
});

// Watch tab switch to init bar chart
import { watch } from 'vue';
watch(activeTab, (val) => {
  if (val === 'bar') {
    nextTick(initBarChart);
  }
});
</script>

<style scoped>
.teacher-analytics-page { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.chart-container { padding: 20px; }
.summary-card { text-align: center; padding: 20px 0; }
.summary-value { font-size: 36px; font-weight: 700; color: #409EFF; }
.summary-label { font-size: 14px; color: #909399; margin-top: 8px; }
</style>
