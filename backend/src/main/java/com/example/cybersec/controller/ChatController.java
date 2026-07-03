package com.example.cybersec.controller;

import com.example.cybersec.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  @PostMapping
  public ResponseEntity<?> chat(@RequestBody Map<String, String> body) {
    String message = body.getOrDefault("message", "");
    String reply = chatService.reply(message);
    return ResponseEntity.ok(Map.of("reply", reply));
  }
}

