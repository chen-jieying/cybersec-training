<template>
  <div class="chat-simulator">
    <div class="chat-status-bar">
      <el-tag v-if="aiAvailable" type="success" size="small" effect="plain">
        AI教练在线（DeepSeek）
      </el-tag>
      <el-tag v-else type="warning" size="small" effect="plain">
        关键词匹配模式
      </el-tag>
      <el-select v-model="sceneType" size="small" style="width:160px; margin-left:8px" @change="resetChat">
        <el-option label="钓鱼邮件诈骗" value="phishing" />
        <el-option label="账户安全诈骗" value="account" />
        <el-option label="冒充公检法" value="fraud" />
        <el-option label="社交工程攻击" value="social" />
      </el-select>
    </div>

    <div class="chat-history" ref="chatHistoryRef">
      <div v-for="(item, index) in messages" :key="index" :class="['message-row', item.role]">
        <div class="message-label">{{ item.role === 'user' ? '我' : 'AI教练' }}</div>
        <div class="message-bubble" v-html="formatContent(item.text)"></div>
      </div>
      <div v-if="loading" class="message-row bot">
        <div class="message-label">AI教练</div>
        <div class="message-bubble typing-indicator">
          <span class="typing-dot"></span><span class="typing-dot"></span><span class="typing-dot"></span>
        </div>
      </div>
    </div>

    <div class="chat-form" v-if="!ended">
      <el-input
        type="textarea"
        v-model="input"
        :rows="3"
        placeholder="请输入你的应答或提问，描述你会如何应对这种情况..."
        @keydown.enter.exact="handleSubmit"
      />
      <div class="form-actions">
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          <el-icon><Promotion /></el-icon> 发送
        </el-button>
        <el-button @click="resetChat">重新开始</el-button>
      </div>
    </div>

    <div v-if="ended" class="chat-ended">
      <el-result icon="success" title="对话已完成" sub-title="感谢你的参与，实训记录已保存。">
        <template #extra>
          <el-button type="primary" @click="resetChat">再来一次</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { Promotion } from '@element-plus/icons-vue';
import { sendChatMessage, checkAiStatus } from '../api';

const messages = ref<{ role: string; text: string }[]>([]);
const input = ref('');
const loading = ref(false);
const ended = ref(false);
const turnNumber = ref(0);
const sceneType = ref('phishing');
const aiAvailable = ref(false);
const chatHistoryRef = ref<HTMLElement | null>(null);

const formatContent = (content: string) => {
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>');
};

const initChat = () => {
  const openings: Record<string, string> = {
    phishing: '您好，我是XX银行的客服。系统检测到您的银行卡在下午有一笔境外消费，如果不是您本人操作，请立即点击链接冻结账户。\n\n（你会怎么做？）',
    account: '警告！您的微信账户在陌生设备上登录。如果不是您本人操作，请立即提供手机验证码锁定账户。\n\n（你会怎么做？）',
    fraud: '您好，我是公安局刑警支队民警。您的身份证被冒用涉嫌重大网络诈骗案，为证明清白，请将资金转入安全核查账户。\n\n（面对这种情况，你会怎么应对？）',
    social: '嗨！我是学习群里的同学~我最近在玩一款游戏需要手机号注册，你的手机号能借我用一下吗？就收个验证码就行！\n\n（面对这种搭讪，你会怎么回应？）',
  };
  messages.value = [{
    role: 'bot',
    text: openings[sceneType.value] || openings['phishing'],
  }];
};

const handleSubmit = async () => {
  if (!input.value.trim() || loading.value) return;

  messages.value.push({ role: 'user', text: input.value });
  const userText = input.value;
  input.value = '';
  turnNumber.value++;

  loading.value = true;
  await scrollToBottom();

  try {
    const response = await sendChatMessage(userText, sceneType.value, turnNumber.value);
    messages.value.push({ role: 'bot', text: response.data.reply });
    aiAvailable.value = response.data.aiAvailable !== false;

    if (turnNumber.value >= 6) {
      ended.value = true;
    }
  } catch {
    messages.value.push({ role: 'bot', text: '系统暂时无法响应，请稍后再试。' });
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
};

const resetChat = () => {
  ended.value = false;
  turnNumber.value = 0;
  messages.value = [];
  input.value = '';
  initChat();
};

const scrollToBottom = async () => {
  await nextTick();
  if (chatHistoryRef.value) {
    chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight;
  }
};

const checkAi = async () => {
  try {
    const res = await checkAiStatus();
    aiAvailable.value = res.data.status === 'healthy';
  } catch {
    aiAvailable.value = false;
  }
};

onMounted(() => {
  checkAi();
  initChat();
});
</script>

<style scoped>
.chat-simulator { background: #fff; padding: 20px; border-radius: 8px; }
.chat-status-bar { display: flex; align-items: center; margin-bottom: 16px; }
.chat-history { max-height: 450px; overflow-y: auto; margin-bottom: 16px; padding: 0 8px; }
.message-row { display: flex; margin-bottom: 16px; }
.message-row.user { justify-content: flex-end; }
.message-label { min-width: 60px; font-size: 12px; color: #999; margin-right: 10px; align-self: flex-end; }
.message-row.user .message-label { order: 2; margin-left: 10px; margin-right: 0; text-align: right; }
.message-bubble { max-width: 70%; padding: 12px 14px; border-radius: 16px; background: #f2f6ff; line-height: 1.6; word-break: break-word; }
.message-row.user .message-bubble { background: #409eff; color: #fff; }
.chat-form { display: flex; flex-direction: column; gap: 10px; }
.form-actions { display: flex; gap: 8px; justify-content: flex-end; }
.chat-ended { padding: 20px; }
.typing-indicator { display: flex; align-items: center; gap: 4px; padding: 14px 18px; }
.typing-dot { width: 7px; height: 7px; border-radius: 50%; background: #a0c0e0; animation: typingBounce 1.4s infinite ease-in-out both; }
.typing-dot:nth-child(1) { animation-delay: -0.32s; }
.typing-dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes typingBounce { 0%, 80%, 100% { transform: scale(0.4); } 40% { transform: scale(1); } }
</style>
