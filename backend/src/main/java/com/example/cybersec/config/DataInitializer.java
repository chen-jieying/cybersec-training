package com.example.cybersec.config;

import com.example.cybersec.model.Question;
import com.example.cybersec.model.ScenarioScript;
import com.example.cybersec.model.SchoolClass;
import com.example.cybersec.model.TeachingResource;
import com.example.cybersec.model.User;
import com.example.cybersec.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据初始化器 - 在首次启动时创建基础数据
 * 使用 H2 文件持久化数据库，数据在应用重启后不会丢失
 * 仅在对应数据不存在时才插入，避免重复初始化
 * 管理员/教师/学生账号初始化后，可在系统中添加新用户并登录
 */
@Configuration
public class DataInitializer {

  @Bean
  public CommandLineRunner initData(
      UserRepository userRepository,
      SchoolClassRepository schoolClassRepository,
      ScenarioScriptRepository scenarioScriptRepository,
      TeachingResourceRepository teachingResourceRepository,
      QuestionRepository questionRepository) {
    return args -> {
      System.out.println("===== 数据初始化检查开始 =====");

      // ========== 基础用户账号 ==========
      if (userRepository.findByUsername("admin1").isEmpty()) {
        userRepository.save(new User(null, "admin1", "123456", "admin",
            "系统管理员", "None", null, "管理员", "13800000001"));
        System.out.println("[初始化] 创建管理员账号: admin1/123456");
      } else {
        System.out.println("[跳过] 管理员账号已存在");
      }
      if (userRepository.findByUsername("teacher1").isEmpty()) {
        userRepository.save(new User(null, "teacher1", "123456", "teacher",
            "李老师", "初二", null, "高级教师", "13800000002"));
        System.out.println("[初始化] 创建教师账号: teacher1/123456");
      } else {
        System.out.println("[跳过] 教师1账号已存在");
      }
      if (userRepository.findByUsername("teacher2").isEmpty()) {
        userRepository.save(new User(null, "teacher2", "123456", "teacher",
            "王老师", "初一", null, "中级教师", "13800000003"));
        System.out.println("[初始化] 创建教师账号: teacher2/123456");
      } else {
        System.out.println("[跳过] 教师2账号已存在");
      }
      // 示例学生（真实数据由教师添加）
      if (userRepository.findByUsername("student1").isEmpty()) {
        userRepository.save(new User(null, "student1", "123456", "student",
            "张三", "初二", 1L, null, ""));
        System.out.println("[初始化] 创建学生账号: student1/123456");
      } else {
        System.out.println("[跳过] 学生1账号已存在");
      }
      if (userRepository.findByUsername("student2").isEmpty()) {
        userRepository.save(new User(null, "student2", "123456", "student",
            "李四", "初二", 1L, null, ""));
        System.out.println("[初始化] 创建学生账号: student2/123456");
      } else {
        System.out.println("[跳过] 学生2账号已存在");
      }

      // ========== 班级数据 ==========
      if (schoolClassRepository.count() == 0) {
        // teacher1(id=2) 负责的班级
        schoolClassRepository.save(new SchoolClass(null, "初二", "初二(2)班", 2L, "李老师"));
        schoolClassRepository.save(new SchoolClass(null, "初二", "初二(3)班", 2L, "李老师"));
        // teacher2(id=3) 负责的班级
        schoolClassRepository.save(new SchoolClass(null, "初一", "初一(1)班", 3L, "王老师"));
        System.out.println("[初始化] 创建3个班级");
      } else {
        System.out.println("[跳过] 班级数据已存在 (" + schoolClassRepository.count() + "个)");
      }

      // ========== 情景剧本 ==========
      if (scenarioScriptRepository.count() == 0) {
        scenarioScriptRepository.save(new ScenarioScript(null, "phishing",
            "钓鱼邮件识别", "模拟收到可疑钓鱼邮件，学习如何识别和应对钓鱼攻击", "内置"));
        scenarioScriptRepository.save(new ScenarioScript(null, "account",
            "账户安全保护", "模拟账户异常登录场景，学习如何保护个人账户安全", "内置"));
        scenarioScriptRepository.save(new ScenarioScript(null, "fraud",
            "电信诈骗防范", "模拟接到诈骗电话，学习如何辨别和应对电信诈骗", "内置"));
        scenarioScriptRepository.save(new ScenarioScript(null, "social",
            "社交工程攻击", "模拟社交平台上的信息窃取场景", "内置"));
        System.out.println("[初始化] 创建4个情景剧本");
      } else {
        System.out.println("[跳过] 情景剧本已存在 (" + scenarioScriptRepository.count() + "个)");
      }

      // ========== 教学资源 ==========
      if (teachingResourceRepository.count() == 0) {
        teachingResourceRepository.save(new TeachingResource(null,
            "网络安全基础知识手册", "pdf", "/files/security-manual.pdf",
            "涵盖网络安全基本概念、常见威胁和防护措施的基础教程", "基础,入门",
            2L, "teacher", "ALL", null, 2L, "2024-01-15"));
        System.out.println("[初始化] 创建1个教学资源");
      } else {
        System.out.println("[跳过] 教学资源已存在 (" + teachingResourceRepository.count() + "个)");
      }

      // ========== 闯关题库（含详细解析） ==========
      if (questionRepository.count() == 0) {
        // 关卡1: 基础知识 (8题)
        questionRepository.save(new Question(null, "基础知识",
            "以下哪项是强密码的最佳实践？", "使用生日作为密码",
            "使用至少12位包含大小写字母、数字和符号的组合",
            "所有账号使用相同密码", "使用简单的单词作为密码",
            "B", "choice", 10,
            "强密码应至少包含12位字符，混合大小写字母、数字和特殊符号。避免使用生日、姓名等个人信息作为密码，不同账号应使用不同密码，防止一个账号泄露导致所有账号被盗。"));

        questionRepository.save(new Question(null, "基础知识",
            "收到陌生链接时，正确的做法是？", "立即点击查看",
            "转发给朋友确认", "不点击，先确认来源可靠性",
            "复制链接在浏览器打开", "C", "choice", 10,
            "陌生链接可能包含恶意程序或钓鱼网站，点击后可能泄露个人信息或感染病毒。正确做法是确认发送者身份和链接来源的可靠性后再决定是否打开。"));

        questionRepository.save(new Question(null, "基础知识",
            "以下哪种行为最容易导致信息泄露？", "使用指纹解锁手机",
            "在公共电脑上勾选\"记住密码\"", "定期更新系统补丁",
            "使用VPN上网", "B", "choice", 10,
            "在公共电脑上勾选\"记住密码\"会使后续使用者可以轻易访问你的账号。公共设备应使用隐私/无痕模式，使用完毕后务必退出登录。"));

        questionRepository.save(new Question(null, "基础知识",
            "以下哪个是正确的网络安全习惯？", "关闭防火墙以提高网速",
            "使用盗版软件节省费用", "定期备份重要数据",
            "随意连接免费WiFi", "C", "choice", 10,
            "定期备份重要数据是应对勒索软件攻击和数据丢失的最有效措施。防火墙不应关闭，盗版软件可能含有恶意代码，公共WiFi存在信息窃取风险。"));

        questionRepository.save(new Question(null, "基础知识",
            "发现电脑中病毒后，首先应该做什么？", "继续正常使用",
            "立即断开网络连接", "重启电脑", "删除可疑文件",
            "B", "choice", 10,
            "发现病毒后应立即断开网络连接，防止病毒通过网络传播或远程控制窃取数据。之后使用杀毒软件扫描查杀，必要时请专业人员处理。"));

        questionRepository.save(new Question(null, "基础知识",
            "以下哪个不是计算机病毒的特征？", "传染性", "潜伏性",
            "可预见性", "破坏性", "C", "choice", 10,
            "计算机病毒的主要特征包括传染性、潜伏性、破坏性、寄生性和触发性。病毒的行为往往无法完全预知，所以\"可预见性\"并非病毒特征。"));

        questionRepository.save(new Question(null, "基础知识",
            "使用公共WiFi时，不建议进行的操作是？", "浏览新闻网站",
            "登录网银进行转账", "查看天气预报", "搜索学习资料",
            "B", "choice", 10,
            "公共WiFi网络可能被黑客监听，进行网银转账等敏感操作时数据可能被截获。建议使用移动数据网络或VPN保护后再进行此类操作。"));

        questionRepository.save(new Question(null, "基础知识",
            "以下哪种密码最安全？", "12345678", "password",
            "Abc@2024#Pk", "abcdefgh", "C", "choice", 10,
            "安全密码应包含大小写字母、数字和特殊符号，长度不少于8位。\"Abc@2024#Pk\" 具备这些特征，而其他选项均为常见弱密码。"));

        // 关卡2: 钓鱼防护 (7题)
        questionRepository.save(new Question(null, "钓鱼防护",
            "钓鱼邮件的典型特征不包括？", "紧迫性语言催促立即行动",
            "发件人地址与官方域名一致", "包含可疑的附件或链接",
            "要求提供个人敏感信息", "B", "choice", 10,
            "钓鱼邮件通常会伪造发件人地址，看起来和官方域名相似但实际略有不同。发件人地址与官方域名完全一致时反而说明可能是真实邮件。"));

        questionRepository.save(new Question(null, "钓鱼防护",
            "收到银行发来的\"账户异常\"邮件要求点击链接验证，应该？",
            "立即点击链接验证", "回复邮件确认",
            "通过官方App或官网客服核实", "忽略该邮件",
            "C", "choice", 10,
            "遇到涉及银行账户的紧急通知，应通过官方渠道（官方App、官方网站、官方客服电话）独立核实，不要直接点击邮件中的链接或回复邮件。"));

        questionRepository.save(new Question(null, "钓鱼防护",
            "以下哪种URL最可能是钓鱼网站？",
            "https://www.icbc.com.cn", "https://www.1cbc.com.cn",
            "https://www.alipay.com", "https://www.weixin.com",
            "B", "choice", 10,
            "钓鱼网站常利用视觉混淆手段，如将字母 \"i\" 替换为数字 \"1\"，将 \"o\" 替换为 \"0\"。仔细检查URL中的每个字符是识别钓鱼网站的基本技巧。"));

        questionRepository.save(new Question(null, "钓鱼防护",
            "收到中奖短信要求先缴纳手续费，这是？",
            "真实的中奖信息", "典型的电信诈骗",
            "营销活动", "系统错误",
            "B", "choice", 10,
            "\"中奖需先缴费\"是典型的电信诈骗手法。正规中奖活动不会要求中奖者提前缴纳任何费用。遇到此类信息应立即删除。"));

        questionRepository.save(new Question(null, "钓鱼防护",
            "如何验证网站的安全性？", "看网站是否好看",
            "检查是否有HTTPS锁标志和正确的域名",
            "看网站访问速度", "随便输入密码测试",
            "B", "choice", 10,
            "HTTPS协议（浏览器地址栏显示锁标志）确保数据传输加密，同时需要确认域名拼写正确。但钓鱼网站也可能使用HTTPS，需结合其他方法判断。"));

        questionRepository.save(new Question(null, "钓鱼防护",
            "以下哪项最能帮助识别钓鱼网站？", "网站使用了漂亮的图片",
            "网站加载速度快", "域名拼写和官方域名不一致",
            "网站有客户评价", "C", "choice", 10,
            "钓鱼网站最常见的特征就是域名与官方域名非常相似但有细微差异，如拼写错误、多余字符等。仔细核对域名是防范钓鱼网站最简单有效的方法。"));

        questionRepository.save(new Question(null, "钓鱼防护",
            "遇到微信好友突然借钱，以下做法最安全的是？",
            "直接转账", "发微信确认", "通过电话或视频核实身份",
            "询问共同朋友", "C", "choice", 10,
            "微信账号可能被盗，仅通过文字消息无法确认对方身份。最安全的方式是通过电话或视频通话直接与本人核实，核实前绝不转账。"));

        // 关卡3: 社交工程 (7题)
        questionRepository.save(new Question(null, "社交工程",
            "陌生人通过社交软件加好友后索要验证码，应该？",
            "给他验证码", "先聊天确认身份",
            "拒绝并提供防诈骗教育", "报告给平台并拉黑",
            "D", "choice", 10,
            "验证码是重要的安全凭证，任何情况下都不应透露给他人。遇到索要验证码的陌生人，应立即拉黑并向平台举报。"));

        questionRepository.save(new Question(null, "社交工程",
            "以下哪种做法可以有效防范社交工程攻击？",
            "在社交平台公开所有个人信息",
            "对陌生人保持警惕，不轻易透露个人信息",
            "接受所有好友申请", "使用真实姓名作为所有平台昵称",
            "B", "choice", 10,
            "社交工程攻击利用人的信任心理获取信息。保持警惕、不轻易透露个人信息、核实对方身份是防范社交工程攻击的基础。"));

        questionRepository.save(new Question(null, "社交工程",
            "有人冒充你的同学向你借钱，你应该？",
            "立即转账", "通过其他方式（如电话）与同学本人确认",
            "先借少量试试", "在群里问大家是否也收到",
            "B", "choice", 10,
            "冒充熟人诈骗是常见的社交工程手法。遇到借钱请求，务必通过其他联系方式与本人直接确认，不要仅凭文字消息就转账。"));

        questionRepository.save(new Question(null, "社交工程",
            "以下哪项信息不适合在社交媒体公开？",
            "兴趣爱好", "身份证号码和家庭住址",
            "喜欢的音乐", "旅游风景照",
            "B", "choice", 10,
            "身份证号码、家庭住址、银行卡号等属于高度敏感个人信息，公开后可能被用于身份盗用、诈骗等违法犯罪活动。"));

        questionRepository.save(new Question(null, "社交工程",
            "收到\"班主任\"发来的缴费通知链接，应该？",
            "直接点击缴费", "先向班主任本人电话确认",
            "转发到班级群", "忽略不管",
            "B", "choice", 10,
            "网络诈骗常冒充学校老师发送缴费通知。收到此类信息必须通过电话或当面与老师本人确认，不点击链接、不转账、不转发未经核实的信息。"));

        questionRepository.save(new Question(null, "社交工程",
            "以下哪项属于社交工程攻击？", "DDoS攻击",
            "SQL注入攻击", "冒充IT部门人员打电话索要密码",
            "暴力破解密码", "C", "choice", 10,
            "社交工程攻击通过欺骗手段获取信息，如冒充权威人员索要密码。DDoS和SQL注入属于技术攻击，暴力破解是密码破解手段，均不属于社交工程。"));

        questionRepository.save(new Question(null, "社交工程",
            "在网上认识陌生人后，哪种行为最危险？",
            "网上聊天交流学习", "分享旅游攻略",
            "约线下见面并告知家庭地址", "讨论兴趣爱好",
            "C", "choice", 10,
            "与网友线下见面存在风险，告知家庭地址更是可能危及人身安全。与陌生网友交流时应保持警惕，避免透露个人隐私和行踪信息。"));

        System.out.println("[初始化] 创建22道闯关题目（含详细解析）");
      } else {
        System.out.println("[跳过] 题库已存在 (" + questionRepository.count() + "道题)");
      }

      System.out.println("===== 数据初始化检查完成 =====");
    };
  }
}
