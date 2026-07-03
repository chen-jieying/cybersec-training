package com.example.cybersec.service;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

  public String reply(String message) {
    String lower = message.toLowerCase();
    if (lower.contains("\u8ba2\u5355") || lower.contains("\u9a8c\u8bc1\u7801") || lower.contains("\u9000\u6b3e")) {
      return "Please do not share verification codes or bank details. Official platforms will not ask for codes in chat.";
    }
    if (lower.contains("\u98ce\u9669")) {
      return "You are being asked to transfer money to unlock an account. Stop and verify with your teacher or parent immediately.";
    }
    if (lower.contains("\u4e2d\u5956") || lower.contains("\u6d3b\u52a8")) {
      return "This is a common scam tactic. Verify the source and do not click unknown links.";
    }
    return "If you encounter suspicious requests, verify official support channels and do not disclose personal information.";
  }
}

