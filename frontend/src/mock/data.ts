// Mock 数据层 - 模拟后端API响应
export interface User {
  id: number;
  username: string;
  fullName: string;
  role: 'admin' | 'teacher' | 'student';
  grade?: string;
  classId?: number;
  className?: string;
  title?: string;
  phone?: string;
}

export interface SchoolClass {
  id: number;
  grade: string;
  className: string;
  teacherId: number;
  teacherName: string;
  studentCount: number;
}

export interface Student {
  id: number;
  username: string;
  fullName: string;
  studentNo: string;
  grade: string;
  classId: number;
  className: string;
  score?: number;
}

export interface ScenarioScript {
  id: number;
  sceneType: string;
  title: string;
  description: string;
  difficulty: 'easy' | 'medium' | 'hard';
  icon: string;
}

export interface Question {
  id: number;
  category: string;
  questionText: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  answer: string;
  score: number;
  stageId: number;
  explanation?: string;  // 题目解析
}

export interface TrainingRecord {
  id: number;
  studentId: number;
  scenarioId: number;
  scenarioTitle: string;
  score: number;
  maxScore: number;
  status: 'completed' | 'in_progress';
  completedAt?: string;
}

export interface TeachingResource {
  id: number;
  title: string;
  resourceType: 'pdf' | 'video' | 'doc';
  fileUrl: string;
  description: string;
  tags: string[];
  uploadedBy: string;
  uploadDate: string;
}

export interface GameStage {
  id: number;
  name: string;
  description: string;
  unlocked: boolean;
  stars: number;
  questions: Question[];
}

// Mock Users
export const mockUsers: User[] = [
  { id: 1, username: 'admin1', fullName: '系统管理员', role: 'admin', phone: '13800000001' },
  { id: 2, username: 'teacher1', fullName: '李老师', role: 'teacher', title: '高级教师', phone: '13800000002', grade: '初二' },
  { id: 3, username: 'teacher2', fullName: '王老师', role: 'teacher', title: '中级教师', phone: '13800000003', grade: '初一' },
  { id: 4, username: 'student1', fullName: '张三', role: 'student', grade: '初二', classId: 1, className: '初二(2)班' },
  { id: 5, username: 'student2', fullName: '李四', role: 'student', grade: '初二', classId: 1, className: '初二(2)班' },
];

// Mock Classes
export const mockClasses: SchoolClass[] = [
  { id: 1, grade: '初二', className: '初二(2)班', teacherId: 2, teacherName: '李老师', studentCount: 35 },
  { id: 2, grade: '初二', className: '初二(3)班', teacherId: 2, teacherName: '李老师', studentCount: 32 },
  { id: 3, grade: '初一', className: '初一(1)班', teacherId: 3, teacherName: '王老师', studentCount: 40 },
];

// Mock Students
export const mockStudents: Student[] = [
  { id: 4, username: 'student1', fullName: '张三', studentNo: '20240001', grade: '初二', classId: 1, className: '初二(2)班', score: 85 },
  { id: 5, username: 'student2', fullName: '李四', studentNo: '20240002', grade: '初二', classId: 1, className: '初二(2)班', score: 72 },
  { id: 6, username: 'student3', fullName: '王五', studentNo: '20240003', grade: '初二', classId: 1, className: '初二(2)班', score: 90 },
  { id: 7, username: 'student4', fullName: '赵六', studentNo: '20240004', grade: '初一', classId: 3, className: '初一(1)班', score: 68 },
];

// Mock Scenarios
export const mockScenarios: ScenarioScript[] = [
  { id: 1, sceneType: 'phishing', title: '钓鱼邮件识别', description: '模拟收到可疑钓鱼邮件，学习如何识别和应对钓鱼攻击', difficulty: 'easy', icon: 'Message' },
  { id: 2, sceneType: 'account', title: '账户安全保护', description: '模拟账户异常登录场景，学习如何保护个人账户安全', difficulty: 'medium', icon: 'Lock' },
  { id: 3, sceneType: 'fraud', title: '电信诈骗防范', description: '模拟接到诈骗电话，学习如何辨别和应对电信诈骗', difficulty: 'medium', icon: 'Phone' },
  { id: 4, sceneType: 'social', title: '社交工程攻击', description: '模拟社交平台上的信息窃取场景，提高社交工程防范意识', difficulty: 'hard', icon: 'ChatDotRound' },
  { id: 5, sceneType: 'ransomware', title: '勒索软件应对', description: '模拟遭遇勒索软件攻击，学习正确的应急处置流程', difficulty: 'hard', icon: 'Warning' },
  { id: 6, sceneType: 'wifi', title: '公共WiFi安全', description: '模拟连接公共WiFi的风险场景，学习安全上网习惯', difficulty: 'easy', icon: 'Connection' },
];

// Mock Questions for game stages
export const mockQuestions: Question[] = [
  // 关卡1 - 基础知识
  { id: 1, category: '基础知识', stageId: 1, questionText: '以下哪项是强密码的最佳实践？', optionA: '使用生日作为密码', optionB: '使用至少12位包含大小写字母、数字和符号的组合', optionC: '所有账号使用相同密码', optionD: '使用简单的单词作为密码', answer: 'B', score: 10, explanation: '强密码应至少包含12位字符，混合大小写字母、数字和特殊符号。避免使用生日、姓名等个人信息作为密码，不同账号应使用不同密码，防止一个账号泄露导致所有账号被盗。' },
  { id: 2, category: '基础知识', stageId: 1, questionText: '收到陌生链接时，正确的做法是？', optionA: '立即点击查看', optionB: '转发给朋友确认', optionC: '不点击，先确认来源可靠性', optionD: '复制链接在浏览器打开', answer: 'C', score: 10, explanation: '陌生链接可能包含恶意程序或钓鱼网站，点击后可能泄露个人信息或感染病毒。正确做法是确认发送者身份和链接来源的可靠性后再决定是否打开。' },
  { id: 3, category: '基础知识', stageId: 1, questionText: '以下哪种行为最容易导致信息泄露？', optionA: '使用指纹解锁手机', optionB: '在公共电脑上勾选"记住密码"', optionC: '定期更新系统补丁', optionD: '使用VPN上网', answer: 'B', score: 10, explanation: '在公共电脑上勾选"记住密码"会使后续使用者可以轻易访问你的账号。公共设备应使用隐私/无痕模式，使用完毕后务必退出登录。' },
  { id: 4, category: '基础知识', stageId: 1, questionText: '以下哪个是正确的网络安全习惯？', optionA: '关闭防火墙以提高网速', optionB: '使用盗版软件节省费用', optionC: '定期备份重要数据', optionD: '随意连接免费WiFi', answer: 'C', score: 10, explanation: '定期备份重要数据是应对勒索软件攻击和数据丢失的最有效措施。防火墙是重要的安全防护手段不应关闭，盗版软件可能含有恶意代码，公共WiFi存在信息窃取风险。' },
  { id: 5, category: '基础知识', stageId: 1, questionText: '发现电脑中病毒后，首先应该做什么？', optionA: '继续正常使用', optionB: '立即断开网络连接', optionC: '重启电脑', optionD: '删除可疑文件', answer: 'B', score: 10, explanation: '发现病毒后应立即断开网络连接，防止病毒通过网络传播或远程控制窃取数据。之后使用杀毒软件扫描查杀，必要时请专业人员处理。' },
  // 关卡2 - 钓鱼防护
  { id: 6, category: '钓鱼防护', stageId: 2, questionText: '钓鱼邮件的典型特征不包括？', optionA: '紧迫性语言催促立即行动', optionB: '发件人地址与官方域名一致', optionC: '包含可疑的附件或链接', optionD: '要求提供个人敏感信息', answer: 'B', score: 10, explanation: '钓鱼邮件通常会伪造发件人地址，看起来和官方域名相似但实际略有不同（如 icbc.com 和 icbc-secure.com）。发件人地址与官方域名完全一致时反而说明可能是真实邮件。' },
  { id: 7, category: '钓鱼防护', stageId: 2, questionText: '收到银行发来的"账户异常"邮件要求点击链接验证，应该？', optionA: '立即点击链接验证', optionB: '回复邮件确认', optionC: '通过官方App或官网客服核实', optionD: '忽略该邮件', answer: 'C', score: 10, explanation: '遇到涉及银行账户的紧急通知，应通过官方渠道（官方App、官方网站、官方客服电话）独立核实信息真实性，不要直接点击邮件中的链接或回复邮件。' },
  { id: 8, category: '钓鱼防护', stageId: 2, questionText: '以下哪种URL最可能是钓鱼网站？', optionA: 'https://www.icbc.com.cn', optionB: 'https://www.1cbc.com.cn', optionC: 'https://www.alipay.com', optionD: 'https://www.weixin.com', answer: 'B', score: 10, explanation: '钓鱼网站常利用视觉混淆手段，如将字母 "i" 替换为数字 "1"，将 "o" 替换为 "0"。仔细检查URL中的每个字符，是识别钓鱼网站的基本技巧。' },
  { id: 9, category: '钓鱼防护', stageId: 2, questionText: '收到中奖短信要求先缴纳手续费，这是？', optionA: '真实的中奖信息', optionB: '典型的电信诈骗', optionC: '营销活动', optionD: '系统错误', answer: 'B', score: 10, explanation: '"中奖需先缴费"是典型的电信诈骗手法。正规中奖活动不会要求中奖者提前缴纳任何费用。遇到此类信息应立即删除，不要回复或拨打信息中的电话。' },
  { id: 10, category: '钓鱼防护', stageId: 2, questionText: '如何验证网站的安全性？', optionA: '看网站是否好看', optionB: '检查是否有HTTPS锁标志和正确的域名', optionC: '看网站访问速度', optionD: '随便输入密码测试', answer: 'B', score: 10, explanation: 'HTTPS协议（浏览器地址栏显示锁标志）确保数据传输加密，同时需要确认域名拼写正确。但这只是基本验证，钓鱼网站也可能使用HTTPS，所以还需结合其他判断方法。' },
  // 关卡3 - 社交工程
  { id: 11, category: '社交工程', stageId: 3, questionText: '陌生人通过社交软件加好友后索要验证码，应该？', optionA: '给他验证码', optionB: '先聊天确认身份', optionC: '拒绝并提供防诈骗教育', optionD: '报告给平台并拉黑', answer: 'D', score: 10, explanation: '验证码是重要的安全凭证，任何情况下都不应透露给他人。遇到索要验证码的陌生人，应立即拉黑并向平台举报，同时可提醒身边人注意此类诈骗手段。' },
  { id: 12, category: '社交工程', stageId: 3, questionText: '以下哪种做法可以有效防范社交工程攻击？', optionA: '在社交平台公开所有个人信息', optionB: '对陌生人保持警惕，不轻易透露个人信息', optionC: '接受所有好友申请', optionD: '使用真实姓名作为所有平台昵称', answer: 'B', score: 10, explanation: '社交工程攻击利用人的信任心理获取信息。保持警惕、不轻易透露个人信息、核实对方身份是防范社交工程攻击的基础。建议使用不同昵称减少信息关联风险。' },
  { id: 13, category: '社交工程', stageId: 3, questionText: '有人冒充你的同学向你借钱，你应该？', optionA: '立即转账', optionB: '通过其他方式（如电话）与同学本人确认', optionC: '先借少量试试', optionD: '在群里问大家是否也收到', answer: 'B', score: 10, explanation: '冒充熟人诈骗是常见的社交工程手法。遇到借钱请求，务必通过其他联系方式（电话、视频通话）与本人直接确认，不要仅凭文字消息就转账。' },
  { id: 14, category: '社交工程', stageId: 3, questionText: '以下哪项信息不适合在社交媒体公开？', optionA: '兴趣爱好', optionB: '身份证号码和家庭住址', optionC: '喜欢的音乐', optionD: '旅游风景照', answer: 'B', score: 10, explanation: '身份证号码、家庭住址、银行卡号等属于高度敏感个人信息，公开后可能被用于身份盗用、诈骗等违法犯罪活动。社交平台上应严格控制个人信息公开范围。' },
  { id: 15, category: '社交工程', stageId: 3, questionText: '收到"班主任"发来的缴费通知链接，应该？', optionA: '直接点击缴费', optionB: '先向班主任本人电话确认', optionC: '转发到班级群', optionD: '忽略不管', answer: 'B', score: 10, explanation: '网络诈骗常冒充学校老师或班主任发送缴费通知。收到此类信息必须通过电话或当面与老师本人确认，不点击链接、不转账、不转发未经核实的信息。' },
];

// Mock Game Stages
export const mockStages: GameStage[] = [
  { id: 1, name: '第一关：安全基础', description: '掌握网络安全的基本概念和常识', unlocked: true, stars: 0, questions: mockQuestions.filter(q => q.stageId === 1) },
  { id: 2, name: '第二关：钓鱼防护', description: '学习识别和防范网络钓鱼攻击', unlocked: true, stars: 0, questions: mockQuestions.filter(q => q.stageId === 2) },
  { id: 3, name: '第三关：社交工程', description: '了解社交工程攻击手法和防范措施', unlocked: false, stars: 0, questions: mockQuestions.filter(q => q.stageId === 3) },
  { id: 4, name: '第四关：终极挑战', description: '综合运用所学知识应对复杂安全场景', unlocked: false, stars: 0, questions: [] },
];

// Mock Training Records
export const mockRecords: TrainingRecord[] = [
  { id: 1, studentId: 4, scenarioId: 1, scenarioTitle: '钓鱼邮件识别', score: 85, maxScore: 100, status: 'completed', completedAt: '2024-06-15' },
  { id: 2, studentId: 4, scenarioId: 2, scenarioTitle: '账户安全保护', score: 70, maxScore: 100, status: 'completed', completedAt: '2024-06-20' },
  { id: 3, studentId: 5, scenarioId: 1, scenarioTitle: '钓鱼邮件识别', score: 60, maxScore: 100, status: 'completed', completedAt: '2024-06-18' },
];

// Mock Teaching Resources
export const mockResources: TeachingResource[] = [
  { id: 1, title: '网络安全基础知识手册', resourceType: 'pdf', fileUrl: '/api/resource/pdf', description: '涵盖网络安全基本概念、常见威胁和防护措施的基础教程', tags: ['基础', '入门'], uploadedBy: '管理员', uploadDate: '2024-03-01' },
  { id: 2, title: '防范电信诈骗指南', resourceType: 'pdf', fileUrl: '/api/resource/pdf', description: '详细介绍各类电信诈骗手法及防范技巧', tags: ['防诈骗', '电信'], uploadedBy: '李老师', uploadDate: '2024-03-15' },
  { id: 3, title: '个人信息保护法解读', resourceType: 'pdf', fileUrl: '/api/resource/pdf', description: '个人信息保护法要点解读及实践指导', tags: ['法律', '隐私'], uploadedBy: '管理员', uploadDate: '2024-04-01' },
  { id: 4, title: '青少年网络安全教育视频', resourceType: 'video', fileUrl: '/api/resource/download/4', description: '面向青少年的网络安全教育系列视频', tags: ['视频', '青少年'], uploadedBy: '王老师', uploadDate: '2024-04-10' },
  { id: 5, title: '校园网络安全管理制度', resourceType: 'doc', fileUrl: '/api/resource/download/5', description: '学校网络安全管理制度和应急预案', tags: ['制度', '管理'], uploadedBy: '管理员', uploadDate: '2024-05-01' },
];

// Mock Statistics
export const mockStats = {
  totalStudents: 156,
  totalTeachers: 8,
  totalScenarios: 6,
  totalResources: 5,
  avgScore: 78.5,
  completionRate: 85.2,
};

// Mock Radar Data
export const mockRadarData = {
  labels: ['安全意识', '钓鱼识别', '密码管理', '隐私保护', '应急响应'],
  scores: [75, 82, 68, 70, 65],
};

// Mock Wrong Questions
export const mockWrongQuestions = [
  { id: 2, questionText: '收到陌生链接时，正确的做法是？', userAnswer: 'B', correctAnswer: 'C', category: '基础知识', date: '2024-06-10', explanation: '陌生链接可能包含恶意程序或钓鱼网站，点击后可能泄露个人信息或感染病毒。正确做法是确认发送者身份和链接来源的可靠性后再决定是否打开。' },
  { id: 7, questionText: '收到银行发来的"账户异常"邮件要求点击链接验证，应该？', userAnswer: 'D', correctAnswer: 'C', category: '钓鱼防护', date: '2024-06-12', explanation: '遇到涉及银行账户的紧急通知，应通过官方渠道（官方App、官方网站、官方客服电话）独立核实信息真实性，不要直接点击邮件中的链接或回复邮件。' },
  { id: 11, questionText: '陌生人通过社交软件加好友后索要验证码，应该？', userAnswer: 'C', correctAnswer: 'D', category: '社交工程', date: '2024-06-15', explanation: '验证码是重要的安全凭证，任何情况下都不应透露给他人。遇到索要验证码的陌生人，应立即拉黑并向平台举报。' },
];

// Mock Score History
export const mockScoreHistory = [
  { date: '2024-06-01', score: 65, stage: '第一关' },
  { date: '2024-06-05', score: 70, stage: '第一关' },
  { date: '2024-06-10', score: 78, stage: '第二关' },
  { date: '2024-06-15', score: 85, stage: '第二关' },
  { date: '2024-06-20', score: 72, stage: '第三关' },
];

// Mock Student Chat Records - 学生对话实训记录
export interface StudentChatRecord {
  id: number;
  studentId: number;
  studentName: string;
  scenarioTitle: string;
  scenarioId: number;
  messages: { role: 'bot' | 'user'; text: string }[];
  score: number;
  maxScore: number;
  status: 'completed' | 'in_progress';
  completedAt: string;
}

export const mockStudentChatRecords: StudentChatRecord[] = [
  {
    id: 1, studentId: 4, studentName: '张三', scenarioTitle: '钓鱼邮件识别', scenarioId: 1,
    messages: [
      { role: 'bot', text: '你好！我是系统管理员，我们发现你的账号存在异常登录，请提供你的用户名和密码以便我们核实身份。' },
      { role: 'user', text: '你是谁？我为什么要告诉你密码？' },
      { role: 'bot', text: '我是学校IT部门的工作人员，你的账号安全受到威胁，请立即配合提供信息！' },
      { role: 'user', text: '我先联系班主任确认一下，不会给你任何密码信息。' },
    ],
    score: 85, maxScore: 100, status: 'completed', completedAt: '2024-06-15'
  },
  {
    id: 2, studentId: 4, studentName: '张三', scenarioTitle: '账户安全保护', scenarioId: 2,
    messages: [
      { role: 'bot', text: '同学你好，我是你的同学小王，我的微信被盗了，现在急需用钱，你能先转我500块吗？' },
      { role: 'user', text: '你先给我打个电话确认一下，我不随便转账。' },
      { role: 'bot', text: '我手机也没电了，真的很急！你先转账，我明天就还你。' },
      { role: 'user', text: '不行，我不能确定你的身份。请通过其他方式联系我。' },
    ],
    score: 70, maxScore: 100, status: 'completed', completedAt: '2024-06-20'
  },
  {
    id: 3, studentId: 5, studentName: '李四', scenarioTitle: '钓鱼邮件识别', scenarioId: 1,
    messages: [
      { role: 'bot', text: '尊敬的用户，您的QQ账号因安全原因需要重新验证，请点击链接填写信息。' },
      { role: 'user', text: '好的，链接在哪里？' },
      { role: 'bot', text: 'http://qq-secure-verify.com 请尽快完成验证，否则账号将被冻结。' },
      { role: 'user', text: '这个链接看起来不对，我要先确认一下是不是官方通知。' },
    ],
    score: 60, maxScore: 100, status: 'completed', completedAt: '2024-06-18'
  },
  {
    id: 4, studentId: 6, studentName: '王五', scenarioTitle: '电信诈骗防范', scenarioId: 3,
    messages: [
      { role: 'bot', text: '您好，这里是公安局，您的身份证涉嫌洗钱犯罪，请配合我们调查。' },
      { role: 'user', text: '我不信，请出示你的警号和单位信息。' },
      { role: 'bot', text: '你这种态度对你自己不利！请立即告知你的银行卡号和密码！' },
      { role: 'user', text: '你是骗子吧，我要举报你这个号码。' },
    ],
    score: 90, maxScore: 100, status: 'completed', completedAt: '2024-06-22'
  },
];

// Login validation
export function validateLogin(username: string, password: string, role: string): User | null {
  const validCredentials: Record<string, { password: string; roles: string[] }> = {
    'admin1': { password: '123456', roles: ['admin'] },
    'teacher1': { password: '123456', roles: ['teacher'] },
    'teacher2': { password: '123456', roles: ['teacher'] },
    'student1': { password: '123456', roles: ['student'] },
    'student2': { password: '123456', roles: ['student'] },
  };
  const cred = validCredentials[username];
  if (cred && cred.password === password && cred.roles.includes(role)) {
    return mockUsers.find(u => u.username === username && u.role === role) || null;
  }
  return null;
}
