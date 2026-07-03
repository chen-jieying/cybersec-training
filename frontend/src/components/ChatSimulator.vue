<template>
  <div class="chat-simulator">
    <div class="chat-history">
      <div v-for="(item, index) in messages" :key="index" :class="['message-row', item.role]">
        <div class="message-label">{{ item.role === 'user' ? '学生' : '系统' }}</div>
        <div class="message-bubble">{{ item.text }}</div>
      </div>
    </div>
    <el-form class="chat-form" @submit.prevent="handleSubmit">
      <el-form-item>
        <el-input
          type="textarea"
          v-model="input"
          placeholder="请输入你的应答或提问，识别诈骗意图..."
          rows="3"
        />
      </el-form-item>
      <el-button type="primary" @click="handleSubmit">发送</el-button>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { sendChatMessage } from '../api';

const messages = ref([
  {
    role: 'bot',
    text: '你好！我是客服，请告诉我你的订单号，我们先核实身份。'
  }
]);
const input = ref('');

const handleSubmit = async () => {
  if (!input.value.trim()) return;
  messages.value.push({ role: 'user', text: input.value });
  const userText = input.value;
  input.value = '';
  try {
    const response = await sendChatMessage(userText);
    messages.value.push({ role: 'bot', text: response.data.reply });
  } catch (error) {
    messages.value.push({ role: 'bot', text: '系统暂时无法响应，请稍后再试。' });
  }
};
</script>

<style scoped>
.chat-simulator {
  background: #fff;
  padding: 20px;
}
.chat-history {
  max-height: 450px;
  overflow-y: auto;
  margin-bottom: 16px;
}
.message-row {
  display: flex;
  margin-bottom: 14px;
}
.message-row.user {
  justify-content: flex-end;
}
.message-label {
  min-width: 60px;
  font-size: 12px;
  color: #999;
  margin-right: 10px;
  align-self: flex-end;
}
.message-bubble {
  max-width: 70%;
  padding: 12px 14px;
  border-radius: 16px;
  background: #f2f6ff;
  line-height: 1.6;
}
.message-row.user .message-bubble {
  background: #409eff;
  color: #fff;
}
.chat-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
