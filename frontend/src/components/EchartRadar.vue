<template>
  <div class="radar-box" ref="chartRef"></div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps<{ data: { labels: string[]; scores: number[] } }>();
const chartRef = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | null = null;

const renderChart = () => {
  if (!chartRef.value) return;
  chart = echarts.init(chartRef.value);
  chart.setOption({
    tooltip: {},
    radar: {
      indicator: props.data.labels.map((label, index) => ({ name: label, max: 100 }))
    },
    series: [
      {
        name: '学情雷达图',
        type: 'radar',
        data: [{ value: props.data.scores, name: '能力得分' }]
      }
    ]
  });
};

watch(() => props.data, renderChart, { immediate: true, deep: true });

onMounted(renderChart);

onBeforeUnmount(() => {
  chart?.dispose();
});
</script>

<style scoped>
.radar-box {
  height: 360px;
  width: 100%;
}
</style>
