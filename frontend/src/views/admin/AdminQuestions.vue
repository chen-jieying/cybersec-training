<template>
  <div class="admin-questions-page">
    <el-card shadow="hover">
      <div class="page-header">
        <div>
          <h3>题库管理</h3>
          <p>管理闯关题库中的题目</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="showAdd = true">
            <el-icon><Plus /></el-icon> 新增题目
          </el-button>
        </div>
      </div>

      <!-- 分类筛选 -->
      <el-row :gutter="16" style="margin-bottom: 16px;">
        <el-col :span="6">
          <el-select v-model="filterCategory" placeholder="按分类筛选" clearable style="width:100%">
            <el-option label="基础知识" value="基础知识" />
            <el-option label="钓鱼防护" value="钓鱼防护" />
            <el-option label="社交工程" value="社交工程" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filterStage" placeholder="按关卡筛选" clearable style="width:100%">
            <el-option label="第一关" :value="1" />
            <el-option label="第二关" :value="2" />
            <el-option label="第三关" :value="3" />
          </el-select>
        </el-col>
      </el-row>

      <el-table :data="filteredQuestions" border stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="questionText" label="题目内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="answer" label="答案" width="80" align="center" />
        <el-table-column prop="score" label="分值" width="80" align="center" />
        <el-table-column prop="stageId" label="关卡" width="80" align="center">
          <template #default="{ row }">第{{ row.stageId }}关</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="editQuestion(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="deleteQuestion(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑题目弹窗 -->
    <el-dialog v-model="showAdd" :title="editingId ? '编辑题目' : '新增题目'" width="700px" :close-on-click-modal="false">
      <el-form :model="questionForm" label-width="100px">
        <el-form-item label="分类">
          <el-select v-model="questionForm.category" placeholder="请选择分类">
            <el-option label="基础知识" value="基础知识" />
            <el-option label="钓鱼防护" value="钓鱼防护" />
            <el-option label="社交工程" value="社交工程" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属关卡">
          <el-select v-model="questionForm.stageId" placeholder="请选择关卡">
            <el-option label="第一关" :value="1" />
            <el-option label="第二关" :value="2" />
            <el-option label="第三关" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input v-model="questionForm.questionText" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="选项A">
          <el-input v-model="questionForm.optionA" />
        </el-form-item>
        <el-form-item label="选项B">
          <el-input v-model="questionForm.optionB" />
        </el-form-item>
        <el-form-item label="选项C">
          <el-input v-model="questionForm.optionC" />
        </el-form-item>
        <el-form-item label="选项D">
          <el-input v-model="questionForm.optionD" />
        </el-form-item>
        <el-form-item label="正确答案">
          <el-select v-model="questionForm.answer">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
            <el-option label="D" value="D" />
          </el-select>
        </el-form-item>
        <el-form-item label="分值">
          <el-input-number v-model="questionForm.score" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="confirmSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { mockQuestions, Question } from '../../mock/data';

const questions = ref<Question[]>([]);
const filterCategory = ref('');
const filterStage = ref<number | ''>('');
const showAdd = ref(false);
const editingId = ref<number | null>(null);

const questionForm = ref({
  category: '',
  stageId: 1,
  questionText: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  answer: 'A',
  score: 10,
});

const filteredQuestions = computed(() => {
  return questions.value.filter(q => {
    if (filterCategory.value && q.category !== filterCategory.value) return false;
    if (filterStage.value && q.stageId !== filterStage.value) return false;
    return true;
  });
});

const loadQuestions = () => {
  questions.value = [...mockQuestions];
};

const editQuestion = (row: Question) => {
  editingId.value = row.id;
  questionForm.value = { ...row };
  showAdd.value = true;
};

const confirmSave = () => {
  if (!questionForm.value.questionText) {
    ElMessage.warning('请输入题目内容');
    return;
  }
  if (editingId.value) {
    const idx = questions.value.findIndex(q => q.id === editingId.value);
    if (idx >= 0) {
      questions.value[idx] = { ...questionForm.value, id: editingId.value };
    }
    ElMessage.success('题目更新成功');
  } else {
    const newQ: Question = { ...questionForm.value, id: Date.now() };
    questions.value.push(newQ);
    ElMessage.success('题目添加成功');
  }
  showAdd.value = false;
  editingId.value = null;
  questionForm.value = { category: '', stageId: 1, questionText: '', optionA: '', optionB: '', optionC: '', optionD: '', answer: 'A', score: 10 };
};

const deleteQuestion = async (row: Question) => {
  try {
    await ElMessageBox.confirm('确定要删除该题目吗？', '删除确认', { type: 'warning' });
    questions.value = questions.value.filter(q => q.id !== row.id);
    ElMessage.success('删除成功');
  } catch { /* cancelled */ }
};

onMounted(loadQuestions);
</script>

<style scoped>
.admin-questions-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { margin: 0 0 4px 0; }
.page-header p { margin: 0; color: #909399; font-size: 14px; }
.header-actions { display: flex; gap: 8px; }
</style>
