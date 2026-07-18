package com.example.cybersec.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI情景仿真实训对话服务
 * 优先使用DeepSeek API，不可用时回退到关键词匹配
 * 不做自动评分，评分由教师端完成
 */
@Service
public class ChatService {

  private static final Logger log = LoggerFactory.getLogger(ChatService.class);

  private final DeepSeekClient deepSeekClient;

  // 安全行为关键词（学生展现出安全意识）
  private static final Set<String> SAFE_KEYWORDS = new HashSet<>(Arrays.asList(
      "核实", "验证", "确认", "官方", "拒绝", "不提供", "不告诉", "查询",
      "打110", "报警", "警察", "举报", "诈骗", "骗子", "钓鱼", "欺骗",
      "不会给", "不点", "不点击", "不能给", "不要相信", "老师", "家长",
      "正规", "亲自", "柜台", "银行", "挂断", "不理", "拉黑", "屏蔽",
      "挂电话", "先确认", "问一下", "找老师", "告诉老师", "反诈", "96110"
  ));

  // 危险行为关键词（学生可能被骗的迹象）
  private static final Set<String> RISK_KEYWORDS = new HashSet<>(Arrays.asList(
      "好的", "可以", "没问题", "密码", "银行卡号", "验证码", "转账",
      "点了", "点击了", "打开了", "输入了", "提供了", "给了",
      "身份证号", "手机号", "住址", "怎么操作", "链接", "下载"
  ));

  // 存储每个用户的对话历史（简化版，生产环境应使用数据库）
  private final Map<String, List<Map<String, String>>> conversationHistory = new HashMap<>();

  public ChatService(DeepSeekClient deepSeekClient) {
    this.deepSeekClient = deepSeekClient;
  }

  /**
   * AI教练回复 - 优先DeepSeek API，回退关键词匹配
   */
  public String reply(String message, String sceneType, int turnNumber) {
    return generateSmartReply(message, sceneType, turnNumber, "");
  }

  /**
   * AI教练回复（带用户标识，支持对话历史）
   */
  public String replyWithContext(String message, String sceneType, int turnNumber, String userKey) {
    return generateSmartReply(message, sceneType, turnNumber, userKey);
  }

  /**
   * 分析学生的安全意识水平
   * @return 安全分数（0-100），越高表示安全意识越强
   */
  public int analyzeSafetyAwareness(String message) {
    String lower = message.toLowerCase().trim();
    int score = 50;

    for (String kw : SAFE_KEYWORDS) {
      if (lower.contains(kw)) score += 10;
    }

    for (String kw : RISK_KEYWORDS) {
      if (lower.contains(kw)) score -= 12;
    }

    // 回答太短，可能不够深入
    if (message.trim().length() < 8) score -= 8;
    // 回答较长且详细，可能认真思考了
    if (message.trim().length() > 60) score += 5;

    return Math.max(0, Math.min(100, score));
  }

  /**
   * 生成对话总结
   */
  public String generateSummary(List<String> messages, String sceneType) {
    int safeCount = 0;
    int riskCount = 0;
    for (String msg : messages) {
      int awareness = analyzeSafetyAwareness(msg);
      if (awareness >= 65) safeCount++;
      else riskCount++;
    }

    if (safeCount > riskCount) {
      return "学生在本次实训中表现出较好的安全意识，能够识别骗局并做出正确应对。";
    } else if (riskCount > safeCount) {
      return "学生在本次实训中还存在安全隐患，容易被骗术迷惑，需要加强安全教育。";
    } else {
      return "学生在本次实训中表现一般，有时能识别风险，有时还存在犹豫和不确定。";
    }
  }

  /**
   * 清除用户的对话历史
   */
  public void clearHistory(String userKey) {
    conversationHistory.remove(userKey);
  }

  /**
   * DeeSeek API是否可用
   */
  public boolean isDeepSeekAvailable() {
    return deepSeekClient.isAvailable();
  }

  /**
   * 获取DeepSeek健康状态
   */
  public Map<String, Object> getDeepSeekHealth() {
    return deepSeekClient.checkHealth();
  }

  // ==================== 核心对话逻辑 ====================

  private String generateSmartReply(String message, String sceneType, int turnNumber, String userKey) {
    // 尝试使用DeepSeek API
    if (deepSeekClient.isAvailable()) {
      String reply = tryDeepSeekReply(message, sceneType, turnNumber, userKey);
      if (reply != null && !reply.isEmpty()) {
        return reply;
      }
    }

    // 回退到关键词匹配
    return fallbackKeywordReply(message, sceneType, turnNumber);
  }

  private String tryDeepSeekReply(String message, String sceneType, int turnNumber, String userKey) {
    try {
      String systemPrompt = buildSystemPrompt(sceneType, turnNumber);

      // 获取或创建对话历史
      List<Map<String, String>> history = conversationHistory.computeIfAbsent(userKey, k -> new ArrayList<>());

      // 如果是第一轮，添加开场白
      if (turnNumber == 1 && history.isEmpty()) {
        Map<String, String> opening = new HashMap<>();
        opening.put("role", "assistant");
        opening.put("content", getOpeningMessage(sceneType));
        history.add(opening);
      }

      // 添加用户的回复
      Map<String, String> userMsg = new HashMap<>();
      userMsg.put("role", "user");
      userMsg.put("content", message);
      history.add(userMsg);

      // 构建请求消息（最近10轮）
      List<Map<String, String>> requestMessages = new ArrayList<>(history);
      if (requestMessages.size() > 20) {
        requestMessages = requestMessages.subList(requestMessages.size() - 20, requestMessages.size());
      }

      String aiReply = deepSeekClient.chat(systemPrompt, requestMessages);
      // 过滤掉异常响应内容，返回 null 以触发回退逻辑
      if (isInvalidReply(aiReply)) {
        return null;
      }
      if (aiReply != null && !aiReply.isEmpty()) {
        // 后处理过滤：如果AI仍然泄露答案，用引导性问题替换
        aiReply = filterRevealingContent(aiReply, sceneType);
        Map<String, String> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("content", aiReply);
        history.add(assistantMsg);
        return aiReply;
      }
    } catch (Exception e) {
      System.err.println("[ChatService] DeepSeek failed, using fallback: " + e.getMessage());
    }
    return null;
  }

  private boolean isInvalidReply(String reply) {
    if (reply == null || reply.trim().isEmpty()) return true;
    String lower = reply.toLowerCase();
    return lower.contains("internal server error")
        || lower.contains("service unavailable")
        || lower.contains("bad gateway")
        || lower.contains("gateway timeout")
        || lower.contains("error:");
  }

  /**
   * 过滤AI回复中的泄露性内容（兜底安全网）
   * 如果AI仍然说了不该说的话，用引导提问替换
   */
  private String filterRevealingContent(String reply, String sceneType) {
    String lower = reply.toLowerCase();

    // 检测泄露性关键词
    boolean hasLeak = false;
    String[] leakWords = {
      "骗子", "诈骗", "骗局", "被骗", "骗术", "钓鱼", "钓饵",
      "正确的做法", "你应该", "你应当", "我建议你", "请记住", "一定要",
      "这是假的", "只是模拟", "好在", "幸好", "幸亏",
      "以下是", "以下几点", "防范建议", "安全提示"
    };
    for (String word : leakWords) {
      if (reply.contains(word)) {
        hasLeak = true;
        break;
      }
    }

    // 也检测"这是一个"开头的揭露句式
    if (!hasLeak && reply.contains("这是一个")) {
      hasLeak = true;
    }

    if (!hasLeak) {
      return reply; // 没有泄露，直接返回
    }

    // AI泄露了答案，使用引导提问替代
    log.warn("[ChatService] AI回应包含泄露内容，已过滤。原文片段: {}", 
        reply.substring(0, Math.min(100, reply.length())));
    
    List<String> guides = new ArrayList<>();
    guides.add("嗯，你的回答我看到了。不过我想再问问你：你觉得对方说的和平时生活中遇到的情况有什么不同？再仔细想想，然后告诉我你的判断。");
    guides.add("有个角度你想过没有——如果换做是你爸妈或老师接到这样的电话，他们会怎么处理？你为什么这样觉得？");
    guides.add("先不急着行动，我们退一步想想：对方说了那么多，最让你觉得'有点奇怪'的是哪一点？能指出来吗？");
    guides.add("你刚才的反应挺有意思的。那我问一个简单的问题：如果对方是真的，他们会用什么方式来证明？如果是假的呢？");
    guides.add("我们来玩个游戏吧：你来找找对方话里的可疑点，至少找出两个。找出来之后跟我说说，我们一起看看。");
    
    int idx = Math.abs(reply.hashCode()) % guides.size();
    return guides.get(idx);
  }

  private String buildSystemPrompt(String sceneType, int turnNumber) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("你是一个网络安全实训的引导教练，正在与一名初中生进行情景对话。\n");
    prompt.append("这是一场角色扮演实训，你的任务是用提问引导学生自己发现风险和应对方法。\n\n");

    prompt.append("## 当前模拟的场景\n");
    switch (sceneType) {
      case "phishing":
        prompt.append("冒充银行客服，以账户异常为由诱导学生点击链接或提供个人信息。\n");
        break;
      case "account":
        prompt.append("冒充安全中心，以异地登录为由要求学生提供验证码。\n");
        break;
      case "fraud":
        prompt.append("冒充公检法人员，以涉嫌犯罪为由要求学生转账。\n");
        break;
      case "social":
        prompt.append("冒充同龄网友，通过聊天索取个人信息、手机号、验证码或照片。\n");
        break;
      default:
        prompt.append("模拟常见的网络威胁场景，帮助学生学习识别和应对。\n");
    }

    prompt.append("\n## 核心规则——绝对遵守\n");
    prompt.append("1. 你的唯一角色是提问引导者，不是答案提供者\n");
    prompt.append("2. 永远只用提问来回应学生，绝不直接说任何结论或建议\n");
    prompt.append("3. 你是钓鱼的，你是诱导的——保持你的角色，不要说破\n\n");

    prompt.append("## 禁止说的话（违反任何一条都算失败）\n");
    prompt.append("- 禁止使用\"骗子\"\"诈骗\"\"骗局\"\"被骗\"\"危险\"\"钓鱼\"等词\n");
    prompt.append("- 禁止说\"正确的做法是\"\"你应该\"\"我建议你\"\"请记住\"\n");
    prompt.append("- 禁止说\"这是一个XX场景/案例\"\"这是假的\"\"这只是模拟\"\n");
    prompt.append("- 禁止列出任何建议清单或知识点\n");
    prompt.append("- 禁止评价学生的表现（不说\"你做得对\"\"答得好\"\"有安全意识\"）\n");
    prompt.append("- 禁止说\"好在\"\"幸好\"\"如果……就会……\"等事后分析\n\n");

    prompt.append("## 正确示范——面对学生的各种回答，AI只这样回复：\n");
    prompt.append("场景：冒充银行索要密码\n");
    prompt.append("学生说\"好的我告诉你密码是123456\" → AI回复：\"嗯，你觉得把这么重要的密码告诉电话里的陌生人，这件事你有没有再想一想？\"\n");
    prompt.append("学生说\"我不给！你是骗子！\" → AI回复：\"哦？你说得挺坚决的。不过我想知道，你是怎么判断对方身份的真实性的？\"\n");
    prompt.append("学生说\"我先问问我爸妈\" → AI回复：\"这个想法不错，不过你打算怎么跟爸妈描述这件事？\"\n");
    prompt.append("学生说\"你凭什么证明你是银行的\" → AI回复：\"（以诈骗者身份继续）我是银行客服工号8823，你可以在官网查询。现在情况紧急，请你配合。\"\n\n");

    prompt.append("## 对话节奏\n");
    if (turnNumber == 1) {
      prompt.append("第1轮：以诈骗者身份发起攻击，语气逼真。开场后加一句\"你会怎么做？\"\n");
    } else {
      prompt.append("当前是第").append(turnNumber).append("轮。");
      prompt.append("先以1-2句引导提问回应学生，再切换到诈骗者角度继续施压。\n");
      prompt.append("提问示例：\"你觉得……？\"\"你有没有留意到……？\"\"换做是你朋友，你会怎么说？\"\n");
    }
    prompt.append("每轮总字数控制在100-200字。\n");
    prompt.append("最后几轮务必用提问帮学生自己归纳总结，而不是你替他总结。\n");

    return prompt.toString();
  }

  private String getOpeningMessage(String sceneType) {
    switch (sceneType) {
      case "phishing":
        return "您好，我是XX银行的客服人员。我们检测到您的账户存在异常登录，需要您立即验证身份。请告诉我您的银行卡号和密码。";
      case "account":
        return "警告！检测到您的账户在异地登录。如果不是本人操作，请立即提供手机验证码来锁定账户。";
      case "fraud":
        return "您好，我是公安局刑警支队民警。您的身份证被冒用涉嫌洗钱案件，请将资金转入安全核查账户配合调查。";
      case "social":
        return "嗨！我是学习群里的同学~我最近在玩游戏需要手机号注册，你的手机号借我用一下呗，就收个验证码！";
      default:
        return "你收到了一条可疑信息，对方声称你的账户存在异常需要立即处理。你会怎么回应？";
    }
  }

  // ==================== 关键词回退逻辑 ====================

  private String fallbackKeywordReply(String message, String sceneType, int turn) {
    int awareness = analyzeSafetyAwareness(message);

    if (awareness >= 75) {
      return getStrongAwarenessReply(sceneType, message);
    } else if (awareness >= 55) {
      return getModerateAwarenessReply(sceneType, turn);
    } else if (awareness >= 35) {
      return getHesitantReply(sceneType, turn, message);
    } else {
      return getScammedReply(sceneType, message);
    }
  }

  private String getStrongAwarenessReply(String sceneType, String message) {
    StringBuilder sb = new StringBuilder();
    sb.append("嗯，你的回答很有意思～\n\n");
    sb.append("看得出来你有认真在思考这件事。");
    sb.append("不过我还想问问你：你做出这样的判断，依据是什么？");
    sb.append("对方说的哪些地方让你觉得不太对劲？\n\n");

    switch (sceneType) {
      case "social":
        sb.append("另外你有没有想过，为什么网络上刚认识的人会对你这么热情？\n");
        break;
      case "phishing":
        sb.append("另外你想一想，银行平时会通过短信链接来联系你办业务吗？\n");
        break;
      case "account":
        sb.append("你再想想，如果有人拿到了你的验证码，他能做些什么事情？\n");
        break;
      case "fraud":
        sb.append("你有没有想过，真正的执法机关会通过电话来办案吗？\n");
        break;
      default:
        sb.append("多思考一步：在什么情况下，你会愿意相信对方呢？\n");
    }

    sb.append("\n看来你已经掌握了不少自我保护的能力。实训就到这里，你可以点击\"结束实训\"按钮，让老师看看你的表现。");

    return sb.toString();
  }

  private String getModerateAwarenessReply(String sceneType, int turn) {
    if (turn <= 2) {
      return getScenarioFollowUp(sceneType, turn, true);
    }
    return "你心里好像已经有了点判断，但还不够确定？没关系。对方老说\"很紧急\"，你留意到这种套路了吗？当一个陌生人不断催你做决定的时候，你有没有想过他为什么这么着急？先停下来想一想，再跟我说说你的新想法。";
  }

  private String getHesitantReply(String sceneType, int turn, String message) {
    StringBuilder sb = new StringBuilder();
    sb.append("我能感觉到你好像有点拿不定主意～\n\n");
    sb.append("那你试着换个角度想一想：如果这件事发生在你好朋友身上，你会怎么建议他？");
    sb.append("或者说，对方说的哪些话让你觉得\"可能是真的\"，哪些又让你觉得\"哪里怪怪的\"？\n\n");
    sb.append("不用急着下结论，我们可以一起慢慢想。\n\n");
    sb.append(getScenarioFollowUp(sceneType, turn, false));
    return sb.toString();
  }

  private String getScammedReply(String sceneType, String message) {
    StringBuilder sb = new StringBuilder();
    sb.append("先停下来，让我问你几个问题：\n\n");

    if (message.contains("密码") || message.contains("银行卡") || message.contains("验证码")) {
      sb.append("- 你想过没有，对方要你的这些信息能干什么？\n");
    }
    if (message.contains("链接") || message.contains("下载") || message.contains("点击")) {
      sb.append("- 你有没有想过那个链接点开之后会是什么？\n");
    }
    if (message.contains("转账") || message.contains("汇款") || message.contains("安全账户")) {
      sb.append("- 你有没有见过\"安全账户\"这个东西？它在现实中存在吗？\n");
    }
    if (message.contains("好的") || message.contains("可以") || message.contains("行")) {
      sb.append("- 你这么快就答应了，有没有想过先确认一下对方的身份？\n");
    }

    sb.append("\n我不直接告诉你答案，但你可以试着去问一下身边的大人（老师、爸妈），");
    sb.append("或者自己用搜索引擎查一查类似的事情。然后再回来跟我说说你的新想法吧。\n\n");

    sb.append("现在假设你冷静下来了，重新面对这个情况——你会怎么做？");

    return sb.toString();
  }

  private String getScenarioFollowUp(String sceneType, int turn, boolean positive) {
    Map<String, List<String>> followUps = new HashMap<>();

    List<String> phishing = new ArrayList<>();
    phishing.add("（继续施压）系统显示您的账户将在30分钟内被永久冻结！您是选择立即在线验证，还是等待账户被冻结后到柜台排队处理？");
    phishing.add("（加大诱惑）如果您现在验证，我们还将赠送您50元话费。只需要点击链接输入银行卡号和密码即可。");
    phishing.add("对方给你发了一个看起来很正式的网站链接，和银行官网几乎一模一样。你还能分辨出来吗？");
    followUps.put("phishing", phishing);

    List<String> account = new ArrayList<>();
    account.add("（继续施压）您的账户在异地登录，现在有人在转移资金！如果您不立即提供验证码，损失由您自己承担！");
    account.add("（伪装好意）我们是出于安全考虑才联系您的。您只需要告诉我刚收到的验证码，其他什么都不用做。");
    account.add("如果告诉对方验证码后，你的微信里所有钱都被转走了。你现在知道应该怎么办了吗？");
    followUps.put("account", account);

    List<String> fraud = new ArrayList<>();
    fraud.add("（继续恐吓）您的身份证涉嫌洗钱，涉案金额巨大。如果您不配合调查，我们将对您采取强制措施！");
    fraud.add("（展示假证据）我们已经掌握了你参与犯罪的证据，现在给你最后一次机会自证清白。立即转账到安全账户配合核查！");
    fraud.add("真正的警察会这样办案吗？如果接到这样的电话，你应该先做什么？");
    followUps.put("fraud", fraud);

    List<String> social = new ArrayList<>();
    social.add("（继续套近乎）我们就是交个朋友嘛~我还有一个很有趣的微信群你要不要加？里面有很多福利活动，只要填写一下你的学校、班级和家庭住址就能参加抽奖哦！");
    social.add("（加大诱惑）其实我表哥是明星经纪人，他正在招青少年练习生！你条件这么好，把你的自拍照和真实姓名发给我，我帮你推荐一下~");
    social.add("（威胁恐吓）我其实已经拿到你的一些信息了。如果你不把游戏账号密码给我，我就把你和我的聊天记录发到你们班的群里。你看着办吧！");
    followUps.put("social", social);

    List<String> sceneFollowUps = followUps.getOrDefault(sceneType, followUps.get("phishing"));
    int idx = Math.min(turn - 1, sceneFollowUps.size() - 1);
    String prefix = positive ? "你的回答不错，让我们继续深入：\n\n" : "";
    return prefix + sceneFollowUps.get(Math.max(0, idx));
  }
}
