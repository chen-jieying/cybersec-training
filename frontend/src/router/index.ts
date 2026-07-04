import { createRouter, createWebHashHistory } from 'vue-router';
import Login from '../views/Login.vue';
import Dashboard from '../views/Dashboard.vue';
import ChatSimulation from '../views/ChatSimulation.vue';
import UserManagement from '../views/UserManagement.vue';
import Forbidden from '../views/Forbidden.vue';
import AdminUsers from '../views/admin/AdminUsers.vue';
import AdminQuestions from '../views/admin/AdminQuestions.vue';
import AdminScenarios from '../views/admin/AdminScenarios.vue';
import AdminResources from '../views/admin/AdminResources.vue';
import AdminSettings from '../views/admin/AdminSettings.vue';
import TeacherClasses from '../views/teacher/TeacherClasses.vue';
import TeacherClassStudents from '../views/teacher/TeacherClassStudents.vue';
import TeacherAnalytics from '../views/teacher/TeacherAnalytics.vue';
import TeacherResources from '../views/teacher/TeacherResources.vue';
import TeacherChatView from '../views/teacher/TeacherChatView.vue';
import StudentStages from '../views/student/StudentStages.vue';
import StudentExam from '../views/student/StudentExam.vue';
import StudentExamResult from '../views/student/StudentExamResult.vue';
import StudentScenarios from '../views/student/StudentScenarios.vue';
import StudentChat from '../views/student/StudentChat.vue';
import StudentProfile from '../views/student/StudentProfile.vue';
import StudentResources from '../views/student/StudentResources.vue';

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/dashboard', component: Dashboard, meta: { roles: ['admin', 'teacher', 'student'] } },
  { path: '/simulation', component: ChatSimulation, meta: { roles: ['student', 'teacher'] } },
  { path: '/users', component: UserManagement, meta: { roles: ['admin', 'teacher'] } },
  { path: '/403', component: Forbidden },
  // Admin routes
  { path: '/admin/users', component: AdminUsers, meta: { roles: ['admin'] } },
  { path: '/admin/questions', component: AdminQuestions, meta: { roles: ['admin'] } },
  { path: '/admin/scenarios', component: AdminScenarios, meta: { roles: ['admin'] } },
  { path: '/admin/resources', component: AdminResources, meta: { roles: ['admin'] } },
  { path: '/admin/settings', component: AdminSettings, meta: { roles: ['admin'] } },
  // Teacher routes
  { path: '/teacher/classes', component: TeacherClasses, meta: { roles: ['teacher'] } },
  { path: '/teacher/classes/:classId/students', component: TeacherClassStudents, meta: { roles: ['teacher'] } },
  { path: '/teacher/analytics', component: TeacherAnalytics, meta: { roles: ['teacher'] } },
  { path: '/teacher/resources', component: TeacherResources, meta: { roles: ['teacher'] } },
  { path: '/teacher/chat', component: TeacherChatView, meta: { roles: ['teacher'] } },
  // Student routes
  { path: '/student/stages', component: StudentStages, meta: { roles: ['student'] } },
  { path: '/student/exam/:stageId', component: StudentExam, meta: { roles: ['student'] } },
  { path: '/student/result/:stageId', component: StudentExamResult, meta: { roles: ['student'] } },
  { path: '/student/scenarios', component: StudentScenarios, meta: { roles: ['student'] } },
  { path: '/student/chat/:scenarioId', component: StudentChat, meta: { roles: ['student'] } },
  { path: '/student/profile', component: StudentProfile, meta: { roles: ['student'] } },
  { path: '/student/resources', component: StudentResources, meta: { roles: ['student'] } },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

function getAuth() {
  const user = localStorage.getItem('currentUser');
  const role = localStorage.getItem('currentRole');
  const expiry = localStorage.getItem('tokenExpiry');
  return { user, role, expiry: expiry ? parseInt(expiry, 10) : null };
}

router.beforeEach((to, _from, next) => {
  const { user, role, expiry } = getAuth();
  const now = Date.now();

  if (to.path === '/login' && user && (!expiry || now < expiry)) {
    return next('/dashboard');
  }

  if (to.path !== '/login' && to.path !== '/403') {
    if (!user || !role) {
      return next('/login');
    }
    if (expiry && now >= expiry) {
      localStorage.removeItem('currentUser');
      localStorage.removeItem('currentRole');
      localStorage.removeItem('tokenExpiry');
      return next('/login');
    }
  }

  if (to.meta && (to.meta as any).roles) {
    const allowed: string[] = (to.meta as any).roles;
    if (!allowed.includes(role as string)) {
      return next('/403');
    }
  }

  next();
});

export default router;
