/**
 * API 服务层 - 所有与后端接口的调用
 * 前后端一一对应，不使用 mock 数据
 */
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
});

// 请求拦截器：自动携带角色和用户名
api.interceptors.request.use(config => {
  const role = localStorage.getItem('currentRole');
  const user = localStorage.getItem('currentUser');
  if (role) config.headers['X-User-Role'] = role.toUpperCase();
  if (user) config.headers['X-User-Name'] = user;
  return config;
});

// ==================== 认证 ====================

export function loginApi(credentials: { username: string; password: string; role: string }) {
  return api.post('/auth/login', credentials);
}

// ==================== 管理员 ====================

export function getAdminUsers() {
  return api.get('/admin/users');
}

export function createAdminUser(data: any) {
  return api.post('/admin/users', data);
}

export function updateAdminUser(id: number, data: any) {
  return api.put(`/admin/users/${id}`, data);
}

export function deleteAdminUser(id: number) {
  return api.delete(`/admin/users/${id}`);
}

export function getAdminQuestions() {
  return api.get('/admin/questions');
}

export function createQuestion(data: any) {
  return api.post('/admin/questions', data);
}

export function updateQuestion(id: number, data: any) {
  return api.put(`/admin/questions/${id}`, data);
}

export function deleteQuestion(id: number) {
  return api.delete(`/admin/questions/${id}`);
}

export function getAdminClasses() {
  return api.get('/admin/classes');
}

export function createClass(data: any) {
  return api.post('/admin/classes', data);
}

export function updateClass(id: number, data: any) {
  return api.put(`/admin/classes/${id}`, data);
}

export function deleteClass(id: number) {
  return api.delete(`/admin/classes/${id}`);
}

export function getAdminResources() {
  return api.get('/admin/resources');
}

export function createResource(data: any) {
  return api.post('/admin/resources', data);
}

export function updateResource(id: number, data: any) {
  return api.put(`/admin/resources/${id}`, data);
}

export function deleteResource(id: number) {
  return api.delete(`/admin/resources/${id}`);
}

export function getAdminScenarios() {
  return api.get('/admin/scenarios');
}

export function createScenario(data: any) {
  return api.post('/admin/scenarios', data);
}

export function updateScenario(id: number, data: any) {
  return api.put(`/admin/scenarios/${id}`, data);
}

export function deleteScenario(id: number) {
  return api.delete(`/admin/scenarios/${id}`);
}

// ==================== 教师端 ====================

/** 获取当前教师名下的班级列表 */
export function getTeacherClasses() {
  return api.get('/teacher/classes');
}

/** 获取全部班级 */
export function getAllClasses() {
  return api.get('/teacher/all-classes');
}

/** 获取某班级的学生列表（含真实成绩） */
export function getClassStudents(classId: number) {
  return api.get(`/teacher/classes/${classId}/students`);
}

/** 获取班级统计数据 */
export function getClassStats(classId: number) {
  return api.get(`/teacher/classes/${classId}/stats`);
}

/** 查看某学生的详细答题记录 */
export function getStudentExamDetail(studentId: number) {
  return api.get(`/teacher/student/${studentId}/exam-detail`);
}

/** 获取某学生的对话实训记录 */
export function getStudentChatRecords(studentId: number) {
  return api.get(`/teacher/chat-records/${studentId}`);
}

/** 获取全班对话实训汇总 */
export function getClassChatSummary(classId: number) {
  return api.get(`/teacher/class-chat-summary/${classId}`);
}

/** 获取教师名下所有班级的学生总数 */
export function getTeacherTotalStudents() {
  return api.get('/teacher/total-students');
}

/** 获取学生对话实训的详细消息记录 */
export function getStudentChatMessages(studentId: number) {
  return api.get(`/teacher/chat-messages/${studentId}`);
}

// ==================== 实训任务管理 ====================

/** 获取某班级的实训任务列表 */
export function getClassTasks(classId: number) {
  return api.get('/teacher/tasks', { params: { classId } });
}

/** 发布实训任务到班级 */
export function createTask(data: { classId: number; scenarioId: number; title: string; description: string }) {
  return api.post('/teacher/tasks', data);
}

/** 删除实训任务 */
export function deleteTask(taskId: number) {
  return api.delete(`/teacher/tasks/${taskId}`);
}

/** 获取实训任务的词云数据 */
export function getTaskWordCloud(taskId: number) {
  return api.get(`/teacher/tasks/${taskId}/word-cloud`);
}

/** 获取实训任务中的学生完成情况 */
export function getTaskStudents(taskId: number) {
  return api.get(`/teacher/tasks/${taskId}/students`);
}

/** 获取某学生在某实训任务中的详细回答 */
export function getStudentTaskResponses(taskId: number, studentId: number) {
  return api.get(`/teacher/tasks/${taskId}/students/${studentId}/responses`);
}

export function getTeacherStudents() {
  return api.get('/teacher/students');
}

export function getTeacherScenarios() {
  return api.get('/teacher/scenarios');
}

export function getTeacherRecords(params?: { studentId?: number }) {
  return api.get('/teacher/records', { params });
}

export function saveTrainingRecord(data: any) {
  return api.post('/teacher/records', data);
}

export function getTeacherProfile() {
  return api.get('/teacher/me');
}

export function teacherAddQuestion(data: any) {
  return api.post('/teacher/questions', data);
}

export function teacherUpdateQuestion(id: number, data: any) {
  return api.put(`/teacher/questions/${id}`, data);
}

// ==================== 学生端 ====================

/** 获取学生个人中心数据 */
export function getStudentProfile() {
  return api.get('/student/profile');
}

/** 获取所有闯关关卡 */
export function getStudentStages() {
  return api.get('/student/stages');
}

/** 获取某关卡的题目 */
export function getStageQuestions(stageId: number) {
  return api.get(`/student/questions/${stageId}`);
}

/** 提交考试答卷 */
export function submitExam(data: { stageId: number; answers: { questionId: number; selectedOption: string }[] }) {
  return api.post('/student/exam/submit', data);
}

/** 获取某次考试的结果 */
export function getExamResult(examSessionId: string) {
  return api.get(`/student/exam/result/${examSessionId}`);
}

export function getStudentScenarios() {
  return api.get('/student/scenarios');
}

export function getStudentRecords() {
  return api.get('/student/records');
}

export function submitTrainingRecord(data: any) {
  return api.post('/student/submit', data);
}

// ==================== 资源管理 ====================

export function getResourceList() {
  return api.get('/resource/list');
}

export function previewPdf() {
  return api.get('/resource/pdf', { responseType: 'blob' });
}

export function previewResource(id: number) {
  return api.get(`/resource/preview/${id}`, { responseType: 'blob' });
}

export function downloadResource(id: number) {
  return api.get(`/resource/download/${id}`, { responseType: 'blob' });
}

/** 上传教学资源（支持班级可见性设置） */
export function uploadResource(formData: FormData) {
  return api.post('/resource/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

// ==================== 对话实训 ====================

export function sendChatMessage(message: string) {
  return api.post('/chat', { message });
}

// ==================== 用户导入导出 ====================

export function fetchUsers() {
  return api.get('/users');
}

export function importUsers(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/users/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

export function exportUsers() {
  return api.get('/users/export', { responseType: 'blob' });
}

export default api;
