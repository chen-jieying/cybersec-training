package com.example.cybersec.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

  @GetMapping("/radar")
  public ResponseEntity<?> radar() {
    return ResponseEntity.ok(Map.of(
        "labels", new String[]{"账号安全", "密码管理", "社交防骗", "数据保护", "风险识别"},
        "scores", new int[]{78, 85, 72, 68, 80}
    ));
  }
}

