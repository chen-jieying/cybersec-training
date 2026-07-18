package com.example.cybersec.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * DeepSeek API 客户端 - 免费AI大模型接口
 * 注册地址：https://platform.deepseek.com/
 * 新用户赠送500万tokens，API兼容OpenAI格式
 */
@Service
public class DeepSeekClient {

  private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

  @Value("${deepseek.api-key:}")
  private String apiKey;

  @Value("${deepseek.enabled:false}")
  private boolean enabled;

  @Value("${deepseek.model:deepseek-chat}")
  private String model;

  @Value("${deepseek.base-url:https://api.deepseek.com/v1}")
  private String baseUrl;

  private final RestTemplate restTemplate;

  public DeepSeekClient() {
    this.restTemplate = new RestTemplate();
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(15000);  // 连接超时15秒
    factory.setReadTimeout(60000);     // 读取超时60秒（AI响应可能较慢）
    this.restTemplate.setRequestFactory(factory);
  }

  @PostConstruct
  public void init() {
    if (isAvailable()) {
      log.info("DeepSeek AI客户端初始化成功，模型: {}，基站: {}，API Key: {}...{}",
          model, baseUrl,
          apiKey != null && apiKey.length() > 6 ? apiKey.substring(0, Math.min(6, apiKey.length())) : "***",
          apiKey != null && apiKey.length() > 4 ? apiKey.substring(Math.max(0, apiKey.length() - 4)) : "***");
      // 启动时验证连接
      verifyConnection();
    } else {
      log.warn("DeepSeek AI客户端未配置或已禁用 (enabled={}, hasKey={})，将使用关键词匹配模式",
          enabled, apiKey != null && !apiKey.isEmpty());
    }
  }

  public boolean isAvailable() {
    return enabled && apiKey != null && !apiKey.isEmpty();
  }

  /**
   * 检测DeepSeek API连通性和账户余额状态
   * @return 状态信息
   */
  public Map<String, Object> checkHealth() {
    Map<String, Object> result = new HashMap<>();
    result.put("configured", enabled && apiKey != null && !apiKey.isEmpty());
    result.put("model", model);
    result.put("baseUrl", baseUrl);

    if (!isAvailable()) {
      result.put("status", "unavailable");
      result.put("message", "DeepSeek API未配置或已禁用，API Key为空");
      return result;
    }

    try {
      // 发送一个简单的测试请求检测连通性
      String url = baseUrl + "/chat/completions";
      List<Map<String, String>> testMessages = new ArrayList<>();
      Map<String, String> testMsg = new HashMap<>();
      testMsg.put("role", "user");
      testMsg.put("content", "ping");
      testMessages.add(testMsg);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", model);
      requestBody.put("messages", testMessages);
      requestBody.put("max_tokens", 5);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(apiKey);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

      RestTemplate shortTimeoutTemplate = new RestTemplate();
      SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
      factory.setConnectTimeout(10000);
      factory.setReadTimeout(15000);
      shortTimeoutTemplate.setRequestFactory(factory);

      ResponseEntity<Map> response = shortTimeoutTemplate.postForEntity(url, request, Map.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        result.put("status", "healthy");
        result.put("message", "DeepSeek API连接正常");
      } else {
        result.put("status", "error");
        result.put("message", "DeepSeek API返回异常状态: " + response.getStatusCode());
      }
    } catch (Exception e) {
      String msg = e.getMessage();
      result.put("status", "error");
      if (msg != null && msg.contains("401")) {
        result.put("message", "API Key无效或已过期，请检查DeepSeek账户");
      } else if (msg != null && msg.contains("402")) {
        result.put("message", "DeepSeek账户余额不足，请充值后再使用");
      } else if (msg != null && msg.contains("429")) {
        result.put("message", "API请求频率超限，请稍后再试");
      } else {
        result.put("message", "无法连接DeepSeek API: " + (msg != null ? msg : "未知错误"));
      }
    }
    return result;
  }

  private void verifyConnection() {
    Map<String, Object> health = checkHealth();
    log.info("DeepSeek连接检测结果: status={}, message={}", health.get("status"), health.get("message"));
  }

  /**
   * 调用DeepSeek聊天接口
   * @param systemPrompt 系统提示词
   * @param messages 对话历史（user/assistant交替）
   * @return AI回复内容，失败时返回null
   */
  public String chat(String systemPrompt, List<Map<String, String>> messages) {
    if (!isAvailable()) {
      return null;
    }

    try {
      String url = baseUrl + "/chat/completions";

      // 构建请求体
      List<Map<String, String>> allMessages = new ArrayList<>();
      Map<String, String> sysMsg = new HashMap<>();
      sysMsg.put("role", "system");
      sysMsg.put("content", systemPrompt);
      allMessages.add(sysMsg);
      allMessages.addAll(messages);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", model);
      requestBody.put("messages", allMessages);
      requestBody.put("temperature", 0.7);
      requestBody.put("max_tokens", 1200);
      requestBody.put("top_p", 0.9);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(apiKey);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices != null && !choices.isEmpty()) {
          Map<String, Object> choice = choices.get(0);
          Map<String, String> message = (Map<String, String>) choice.get("message");
          if (message != null) {
            return message.get("content");
          }
        }
      }
      return null;
    } catch (Exception e) {
      log.warn("DeepSeek API调用失败: {}，将回退到关键词匹配", e.getMessage());
      return null;
    }
  }
}
