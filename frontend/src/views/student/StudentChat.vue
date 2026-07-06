<template>
  <div class="student-chat-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>{{ scenarioTitle }}</h3>
          <p>与AI教练进行多轮对话，学习应对各类网络安全威胁</p>
        </div>
        <div class="header-actions">
          <el-tag v-if="trainingEnded" type="info" style="margin-right:8px">实训已完成</el-tag>
          <el-button type="warning" @click="confirmEnd">
            <el-icon><CloseBold /></el-icon> 结束实训
          </el-button>
        </div>
      </div>

      <div class="chat-container">
        <div class="chat-messages" ref="chatContainer">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', msg.role]"
          >
            <div class="message-avatar">
              <el-icon :size="28" v-if="msg.role === 'bot'"><Monitor /></el-icon>
              <el-icon :size="28" v-else><UserFilled /></el-icon>
            </div>
            <div class="message-bubble">
              <div class="message-sender">{{ msg.role === 'bot' ? 'AI教练' : '我' }}</div>
              <div class="message-content" v-html="formatContent(msg.content)"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
          <div v-if="loading" class="message-item bot">
            <div class="message-avatar">
              <el-icon :size="28"><Monitor /></el-icon>
            </div>
            <div class="message-bubble typing">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
        </div>

        <div class="chat-input-area" v-if="!trainingEnded">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入你的回答，描述你会如何应对这种情况..."
            @keydown.enter.exact="sendMessage"
            :disabled="loading"
          />
          <el-button
            type="primary"
            :loading="loading"
            @click="sendMessage"
            style="margin-top:8px;"
          >
            <el-icon><Promotion /></el-icon> 发送
          </el-button>
        </div>

        <div v-if="trainingEnded" class="training-ended">
          <el-result
            icon="success"
            title="实训对话已完成"
            sub-title="你的对话记录已提交给教师进行评阅，成绩由教师评定。"
          >
            <template #extra>
              <el-button type="primary" @click="goBack">返回场景列表</el-button>
              <el-button @click="restartTraining">再做一次</el-button>
            </template>
          </el-result>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CloseBold, Monitor, UserFilled, Promotion } from '@element-plus/icons-vue';
import { getStudentScenarios } from '../../api';
import api from '../../api';

const route = useRoute();
const router = useRouter();
const scenarioId = computed(() => Number(route.params.scenarioId));

const allScenarios = ref<any[]>([]);
const scenario = computed(() => allScenarios.value.find(s => s.id === scenarioId.value));
const scenarioTitle = computed(() => scenario.value?.title || '未知场景');

interface ChatMessage {
  role: 'bot' | 'user';
  content: string;
  time: string;
}

const messages = ref<ChatMessage[]>([]);
const inputMessage = ref('');
const loading = ref(false);
const trainingEnded = ref(false);
const turnNumber = ref(0);
const chatContainer = ref<HTMLElement | null>(null);

// 初始场景开场白
const initChat = () => {
  const sceneType = scenario.value?.sceneType || 'phishing';
  const openings: Record<string, string> = {
    phishing: '【场景：钓鱼邮件/短信】\n\n您好，我是XX银行的客服人员。我们检测到您的账户存在异常登录，需要您立即验证身份以保护您的资金安全。请告诉我您的银行卡号和密码，以便我们进行核查。\n\n（请回答：面对这种情况，你会怎么回应？）',
    account: '【场景：账户异常】\n\n警告！系统检测到您的账户在异地被登录。登录IP：192.168.1.1，地点：XX省XX市。如果不是您本人操作，需要您立即提供手机验证码来证明您的身份并锁定账户。\n\n（请回答：你会怎么应对？）',
    fraud: '【场景：冒充公检法】\n\n您好，我是XX市公安局刑警支队的民警。经查，您的身份证号被他人冒用，涉嫌一起重大洗钱案件。为了证明您的清白，请配合调查，将您的资金转入我们指定的"安全账户"进行核查。\n\n（请回答：面对这种情况，你会怎么办？）',
  };
  messages.value = [{
    role: 'bot',
    content: openings[sceneType] || openings['phishing'],
    time: formatTime(new Date()),
  }];
};

const sendMessage = async () => {
  const text = inputMessage.value.trim();
  if (!text || loading.value) return;

  messages.value.push({
    role: 'user',
    content: text,
    time: formatTime(new Date()),
  });
  inputMessage.value = '';
  turnNumber.value++;
  await scrollToBottom();

  loading.value = true;

  try {
    const res = await api.post('/chat/scenario', {
      message: text,
      sceneType: scenario.value?.sceneType || 'phishing',
      turnNumber: turnNumber.value,
      scenarioId: scenarioId.value,
    });

    const data = res.data;

    messages.value.push({
      role: 'bot',
      content: data.reply,
      time: formatTime(new Date()),
    });

    // 安全意识足够高或对话轮次达到上限，自动完成
    if (data.isTrainingEnd) {
      trainingEnded.value = true;
      // 提交结束
      await api.post('/chat/end-training', {
        scenarioId: scenarioId.value,
        scenarioTitle: scenarioTitle.value,
        sceneType: scenario.value?.sceneType || 'phishing',
        turnCount: turnNumber.value,
      });
      ElMessage.success('恭喜完成实训！记录已提交教师评阅。');
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.error || '对话失败，请重试');
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
};

const confirmEnd = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要结束本次实训吗？你的对话记录将被保存并提交给教师评阅。',
      '结束实训',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '继续对话' }
    );
  } catch { return; }

  loading.value = true;
  try {
    await api.post('/chat/end-training', {
      scenarioId: scenarioId.value,
      scenarioTitle: scenarioTitle.value,
      sceneType: scenario.value?.sceneType || 'phishing',
      turnCount: turnNumber.value,
    });
    trainingEnded.value = true;
    ElMessage.success('实训已结束，记录已提交教师评阅。');
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.error || '结束失败');
  } finally {
    loading.value = false;
  }
};

const goBack = () => router.push('/student/scenarios');

const restartTraining = () => {
  trainingEnded.value = false;
  turnNumber.value = 0;
  messages.value = [];
  initChat();
};

const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

const formatContent = (content: string) => {
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>');
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

const loadScenarios = async () => {
  try {
    const res = await getStudentScenarios();
    allScenarios.value = res.data || [];
  } catch (e) {
    console.error('加载情景剧本失败', e);
  }
};

onMounted(async () => {
  await loadScenarios();
  initChat();
});
</script>

<style scoped>
.student-chat-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.header-actions { display: flex; align-items: center; }
.chat-container { border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.chat-messages { height: 450px; overflow-y: auto; padding: 20px; background: #fafbfc; }
.message-item { display: flex; gap: 12px; margin-bottom: 20px; }
.message-item.user { flex-direction: row-reverse; }
.message-avatar { flex-shrink: 0; width: 36px; height: 36px; border-radius: 50%; background: #e6f0ff; display: flex; align-items: center; justify-content: center; color: #409EFF; }
.message-item.user .message-avatar { background: #e6f7e6; color: #67C23A; }
.message-bubble { max-width: 70%; padding: 12px 16px; border-radius: 12px; background: #fff; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.message-item.bot .message-bubble { border-top-left-radius: 4px; }
.message-item.user .message-bubble { background: #409EFF; color: #fff; border-top-right-radius: 4px; }
.message-sender { font-size: 12px; color: #909399; margin-bottom: 4px; }
.message-item.user .message-sender { color: rgba(255,255,255,0.8); }
.message-content { font-size: 14px; line-height: 1.8; white-space: pre-wrap; word-break: break-word; }
.message-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; text-align: right; }
.typing { display: flex; align-items: center; gap: 4px; padding: 16px 24px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #c0c4cc; animation: typing 1.4s infinite ease-in-out both; }
.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes typing { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1); } }
.chat-input-area { padding: 16px; border-top: 1px solid #ebeef5; background: #fff; text-align: right; }
.training-ended { padding: 40px; background: #fff; }
</style>
