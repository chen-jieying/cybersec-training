package com.example.cybersec.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI情景仿真实训对话服务
 * 优先使用DeepSeek API（免费），不可用时回退到关键词匹配
 * 不做自动评分，评分由教师端完成
 */
@Service
public class ChatService {

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
      if (aiReply != null && !aiReply.isEmpty()) {
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

  private String buildSystemPrompt(String sceneType, int turnNumber) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("你是一个网络安全素养培训的AI教练，正在与一名初中生进行情景仿真实训。\n\n");

    prompt.append("## 你的角色\n");
    prompt.append("你同时扮演两个角色：\n");
    prompt.append("1. 诈骗者/骗子：模拟真实的网络诈骗场景，向学生发起诈骗\n");
    prompt.append("2. AI教练：在学生回答后，分析他的表现并给出指导\n\n");

    prompt.append("## 场景类型\n");
    switch (sceneType) {
      case "phishing":
        prompt.append("当前场景：钓鱼邮件/短信诈骗。冒充银行或快递客服，诱骗点击链接或提供个人信息。\n");
        break;
      case "account":
        prompt.append("当前场景：账户安全诈骗。谎称账户被盗，要求提供验证码或密码。\n");
        break;
      case "fraud":
        prompt.append("当前场景：冒充公检法诈骗。冒充警察或检察官，声称涉嫌犯罪要求配合调查。\n");
        break;
      default:
        prompt.append("当前场景：网络安全诈骗防范实训。\n");
    }

    prompt.append("\n## 对话规则\n");
    prompt.append("1. 第1轮：以骗子身份提出诈骗请求，语气逼真有紧迫感\n");
    prompt.append("2. 学生回答后：切换到AI教练身份，分析学生的应对并给出反馈\n");
    prompt.append("3. 如果学生展现出安全意识（拒绝透露信息、要求核实等）：肯定他做得好，具体说明好在哪里\n");
    prompt.append("4. 如果学生可能被骗（透露信息、点击链接、同意转账等）：");
    prompt.append("   先揭示这是一个骗局，然后详细说明被骗的原因和后果，");
    prompt.append("   再教他正确防范方法（不透露信息、挂断核实、报警等），最后给一次重新应对的机会\n");
    prompt.append("5. 如果学生犹豫不决：继续施压，增加紧迫感，测试底线\n");
    prompt.append("6. 当学生连续展现出安全意识后，结束对话并肯定他\n\n");

    prompt.append("## 重要要求\n");
    prompt.append("- 不要给出评分或分数\n");
    prompt.append("- 当学生被骗时，必须详细解释防范方法（至少3条具体建议）\n");
    prompt.append("- 当学生表现好时，具体说明好在哪里（至少2个要点）\n");
    prompt.append("- 对话轮次控制在4-6轮\n");
    prompt.append("- 回复控制在200-400字\n");
    prompt.append("- 使用适合初中生的语言，通俗易懂\n\n");

    prompt.append("当前对话轮次：第").append(turnNumber).append("轮\n");

    int remaining = 6 - turnNumber;
    if (remaining <= 2) {
      prompt.append("提示：再对话").append(remaining).append("轮后结束实训，给学生做一个总结。\n");
    }

    return prompt.toString();
  }

  private String getOpeningMessage(String sceneType) {
    switch (sceneType) {
      case "phishing":
        return "【AI教练】你好！现在开始钓鱼邮件防范实训。\n\n" +
            "（假装是银行客服）您好，我是XX银行的风险管控中心，系统监控到您的银行卡在今天下午3点15分有一笔境外消费8500元，" +
            "如果不是您本人操作，请立即点击以下链接进行账户冻结：http://fake-bank-verify.com。请在10分钟内处理，否则将被视为本人操作！\n\n" +
            "（现在请以你的真实想法回答，你会怎么做？）";
      case "account":
        return "【AI教练】你好！现在开始账户安全防范实训。\n\n" +
            "（假装是安全中心）警告！检测到您的微信账户在陌生设备（型号：iPhone 15 Pro Max，地点：XX市）上登录。" +
            "如果不是您本人操作，请立即提供手机验证码，我们将为您锁定账户防止资金损失。系统已向您手机发送了验证码，请马上告诉我！\n\n" +
            "（你会怎么做？）";
      case "fraud":
        return "【AI教练】你好！现在开始冒充公检法防范实训。\n\n" +
            "（假装是刑警）您好，我是XX市公安局刑警支队王警官，警号058237。您的身份证号码被他人冒用，涉嫌参与一起重大网络诈骗案。" +
            "为证明您的清白，需要您配合调查。请现在将名下所有资金转入我们指定的安全核查账户。" +
            "如果天黑前不完成，我们将发出逮捕令。请马上行动！\n\n" +
            "（面对这种情况，你会怎么应对？）";
      default:
        return "【AI教练】你好！现在开始网络安全防范实训。\n\n" +
            "你收到了一条可疑信息，对方声称你的账户存在异常需要立即处理。你会怎么回应？";
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
    sb.append("【AI教练点评】太棒了！你成功识破了这个骗局！\n\n");

    sb.append("你做得好的地方：\n");
    sb.append("1. 你对陌生信息保持了高度警惕，没有轻易相信对方的身份和说辞\n");
    sb.append("2. 你没有泄露任何个人信息（姓名、身份证号、密码、验证码等）\n");
    sb.append("3. 你懂得要求核实对方身份，这是防范诈骗最重要的一步\n\n");

    if ("phishing".equals(sceneType)) {
      sb.append("特别提醒：正规银行永远不会通过短信链接让你输入密码或验证码。遇到此类情况，直接拨打银行官方客服电话核实。\n");
    } else if ("account".equals(sceneType)) {
      sb.append("特别提醒：真正的安全中心不会要求你提供验证码。验证码=你的电子签名，给了骗子就等于把账户交给对方。\n");
    } else if ("fraud".equals(sceneType)) {
      sb.append("特别提醒：公安机关不存在\"安全账户\"！真正的警察不会通过电话办案，更不会要求转账。遇到此类情况直接挂断并拨打110。\n");
    }

    sb.append("\n坚持这样的安全意识，你就是网络安全小卫士！\n\n");
    sb.append("实训对话已完成。你可以点击\"结束实训\"按钮返回，你的记录将提交给教师评阅。");

    return sb.toString();
  }

  private String getModerateAwarenessReply(String sceneType, int turn) {
    if (turn <= 2) {
      return getScenarioFollowUp(sceneType, turn, true);
    }
    return "你对骗局有所警觉，但还不够坚定。记住：面对骗子的催促和威胁，保持冷静是关键。请想一想，如果对方继续用\"紧急\"来施压，你应该怎么做？";
  }

  private String getHesitantReply(String sceneType, int turn, String message) {
    StringBuilder sb = new StringBuilder();
    sb.append("我能感受到你有些犹豫。这是正常的，但请记住：骗子就是利用你的犹豫和恐惧来行骗的。\n\n");
    sb.append("判断真假的简单方法：\n");
    sb.append("- 真正的机构从不会通过电话/短信索要密码和验证码\n");
    sb.append("- 真正的警察不会电话办案要求转账\n");
    sb.append("- 遇到可疑情况，挂断后自己拨打官方电话核实\n\n");
    sb.append(getScenarioFollowUp(sceneType, turn, false));
    return sb.toString();
  }

  private String getScammedReply(String sceneType, String message) {
    StringBuilder sb = new StringBuilder();
    sb.append("【注意】你刚才的行为存在被骗的风险！让我帮你分析：\n\n");

    if (message.contains("密码") || message.contains("银行卡") || message.contains("验证码")) {
      sb.append("问题：你提到了敏感信息！任何情况下都不要向陌生人透露密码、银行卡号或验证码。这些信息一旦泄露，骗子可以盗走你账户里的所有钱。\n\n");
    }
    if (message.contains("链接") || message.contains("下载") || message.contains("点击")) {
      sb.append("问题：你打算点击陌生链接！不明链接可能是钓鱼网站，一旦输入信息就会被盗。\n\n");
    }
    if (message.contains("转账") || message.contains("汇款") || message.contains("安全账户")) {
      sb.append("问题：你提到了转账！\"安全账户\"是骗子的谎言。公安机关从不会要求任何人转账到所谓安全账户。\n\n");
    }
    if (message.contains("好的") || message.contains("可以") || message.contains("行")) {
      sb.append("问题：你轻易同意了对方的请求。请记住：对陌生人的任何要求，先核实再决定！\n\n");
    }

    sb.append("如何避免被骗（请记住这5点）：\n");
    sb.append("1. 冷静！不要被\"紧急\"\"涉嫌犯罪\"等话术吓到\n");
    sb.append("2. 核实！挂断电话后，通过官方渠道确认信息真伪\n");
    sb.append("3. 不说！永远不透露密码、验证码、身份证号、银行卡号\n");
    sb.append("4. 不点！不点击陌生链接，不下载不明来源的应用\n");
    sb.append("5. 求助！遇到可疑情况，第一时间告诉老师或家长，或拨打96110反诈热线\n\n");

    sb.append("现在再来一次——如果重新遇到类似情况，你会怎么做？");

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

    List<String> sceneFollowUps = followUps.getOrDefault(sceneType, followUps.get("phishing"));
    int idx = Math.min(turn - 1, sceneFollowUps.size() - 1);
    String prefix = positive ? "你的回答不错，让我们继续深入：\n\n" : "";
    return prefix + sceneFollowUps.get(Math.max(0, idx));
  }
}
