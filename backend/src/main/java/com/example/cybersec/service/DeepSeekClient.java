package com.example.cybersec.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek API 客户端 - 免费AI大模型接口
 * 注册地址：https://platform.deepseek.com/
 * 新用户赠送500万tokens，API兼容OpenAI格式
 */
@Service
public class DeepSeekClient {

  @Value("${deepseek.api-key:}")
  private String apiKey;

  @Value("${deepseek.enabled:false}")
  private boolean enabled;

  @Value("${deepseek.model:deepseek-chat}")
  private String model;

  @Value("${deepseek.base-url:https://api.deepseek.com/v1}")
  private String baseUrl;

  private final RestTemplate restTemplate = new RestTemplate();

  public boolean isAvailable() {
    return enabled && apiKey != null && !apiKey.isEmpty();
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
      System.err.println("[DeepSeek] API call failed: " + e.getMessage());
      return null;
    }
  }
}
