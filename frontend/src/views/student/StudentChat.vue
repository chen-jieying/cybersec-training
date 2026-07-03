<template>
  <div class="student-chat-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>{{ scenarioTitle }}</h3>
          <p>与AI进行多轮对话，完成情景实训</p>
        </div>
        <el-button type="warning" @click="endTraining">
          <el-icon><CloseBold /></el-icon> 结束实训
        </el-button>
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
              <div class="message-content">{{ msg.content }}</div>
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
            placeholder="输入你的回复..."
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
            title="实训已完成"
            :sub-title="`AI评分：${score}分 / ${maxScore}分`"
          >
            <template #extra>
              <el-button type="primary" @click="goBack">返回场景列表</el-button>
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
import { ElMessage } from 'element-plus';
import { CloseBold, Monitor, UserFilled, Promotion } from '@element-plus/icons-vue';
import { mockScenarios } from '../../mock/data';

const route = useRoute();
const router = useRouter();
const scenarioId = computed(() => Number(route.params.scenarioId));
const scenario = computed(() => mockScenarios.find(s => s.id === scenarioId.value));
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
const score = ref(0);
const maxScore = ref(100);
const chatContainer = ref<HTMLElement | null>(null);

const botReplies: Record<string, string[]> = {
  'phishing': [
    '您好，我是XX银行客服。我们检测到您的账户存在异常登录，需要您立即验证身份。请提供您的银行卡号和密码。',
    '这是紧急情况！如果您不在30分钟内验证，您的账户将被冻结。请点击以下链接进行验证：[http://fake-bank.com/verify]',
    '我理解您的顾虑。但为了您的资金安全，请务必配合验证。您也可以拨打我们的客服热线：400-XXX-XXXX。',
  ],
  'account': [
    '警告！您的账户在异地被登录。登录IP地址：192.168.1.1，地点：XX省XX市。如果不是您本人操作，请立即修改密码。',
    '我们建议您开启双重认证。请提供您的手机号码，我们将发送验证码。',
    '您的账户安全级别较低。我们推荐您使用至少12位包含大小写字母、数字和特殊字符的强密码。',
  ],
  'fraud': [
    '您好，我是XX公安局民警。您的身份证被他人冒用涉嫌洗钱犯罪，需要您配合调查。请将您的资金转入我们的"安全账户"。',
    '如果您不配合，我们将对您采取强制措施。这是您的案件编号：2024XXXX，请务必保密。',
    '我理解您可能有所顾虑，但这是为了保护您的权益。您也可以到当地派出所核实。',
  ],
};

const initChat = () => {
  const sceneType = scenario.value?.sceneType || 'phishing';
  const replies = botReplies[sceneType] || botReplies['phishing'];
  messages.value = [{
    role: 'bot',
    content: replies[0],
    time: formatTime(new Date()),
  }];
};

const sendMessage = async () => {
  const text = inputMessage.value.trim();
  if (!text || loading.value) return;

  // Add user message
  messages.value.push({
    role: 'user',
    content: text,
    time: formatTime(new Date()),
  });
  inputMessage.value = '';
  scrollToBottom();

  loading.value = true;

  // Simulate AI response
  await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 1500));

  const sceneType = scenario.value?.sceneType || 'phishing';
  const replies = botReplies[sceneType] || botReplies['phishing'];
  const nextReplyIndex = messages.value.filter(m => m.role === 'bot').length;

  if (nextReplyIndex < replies.length) {
    messages.value.push({
      role: 'bot',
      content: replies[nextReplyIndex],
      time: formatTime(new Date()),
    });
  } else {
    // Training complete
    const aiScore = Math.floor(60 + Math.random() * 40);
    score.value = aiScore;
    messages.value.push({
      role: 'bot',
      content: `实训结束！根据您的表现，AI评分：${aiScore}/100分。您展现了良好的安全意识，请继续保持！`,
      time: formatTime(new Date()),
    });
    trainingEnded.value = true;
  }

  loading.value = false;
  scrollToBottom();
};

const endTraining = () => {
  trainingEnded.value = true;
  score.value = Math.floor(60 + Math.random() * 40);
  ElMessage.info('实训已结束');
};

const goBack = () => {
  router.back();
};

const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

onMounted(initChat);
</script>

<style scoped>
.student-chat-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.chat-container {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}
.chat-messages {
  height: 450px;
  overflow-y: auto;
  padding: 20px;
  background: #fafbfc;
}
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.message-item.user {
  flex-direction: row-reverse;
}
.message-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e6f0ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409EFF;
}
.message-item.user .message-avatar {
  background: #e6f7e6;
  color: #67C23A;
}
.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.message-item.bot .message-bubble {
  border-top-left-radius: 4px;
}
.message-item.user .message-bubble {
  background: #409EFF;
  color: #fff;
  border-top-right-radius: 4px;
}
.message-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.message-item.user .message-sender {
  color: rgba(255,255,255,0.8);
}
.message-content {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.message-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
  text-align: right;
}
.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 16px 24px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: typing 1.4s infinite ease-in-out both;
}
.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
.chat-input-area {
  padding: 16px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  text-align: right;
}
.training-ended {
  padding: 40px;
  background: #fff;
}
</style>
