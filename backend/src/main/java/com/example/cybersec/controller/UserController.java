package com.example.cybersec.controller;

import com.example.cybersec.model.User;
import com.example.cybersec.repository.UserRepository;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<User> listUsers() {
    return userRepository.findAll().stream()
        .map(user -> {
          User safe = new User();
          safe.setId(user.getId());
          safe.setUsername(user.getUsername());
          safe.setPassword("");
          safe.setRole(user.getRole());
          safe.setFullName(user.getFullName());
          safe.setGrade(user.getGrade());
          safe.setClassId(user.getClassId());
          safe.setTitle(user.getTitle());
          safe.setPhone(user.getPhone());
          return safe;
        })
        .collect(Collectors.toList());
  }

  @PostMapping("/import")
  public ResponseEntity<?> importUsers(@RequestParam("file") MultipartFile file) throws IOException {
    try (var workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;
        String username = row.getCell(0).getStringCellValue();
        String role = row.getCell(1).getStringCellValue();
        String fullName = row.getCell(2).getStringCellValue();
        String grade = row.getCell(3).getStringCellValue();
        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");
        user.setRole(role);
        user.setFullName(fullName);
        user.setGrade(grade);
        userRepository.save(user);
      }
    }
    return ResponseEntity.ok().build();
  }

  @GetMapping("/export")
  public void exportUsers(HttpServletResponse response) throws IOException {
    try (var workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("students");
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Username");
      header.createCell(1).setCellValue("角色");
      header.createCell(2).setCellValue("姓名");
      header.createCell(3).setCellValue("年级");

      List<User> users = userRepository.findAll();
      for (int i = 0; i < users.size(); i++) {
        Row row = sheet.createRow(i + 1);
        User user = users.get(i);
        row.createCell(0).setCellValue(user.getUsername());
        row.createCell(1).setCellValue(user.getRole());
        row.createCell(2).setCellValue(user.getFullName());
        row.createCell(3).setCellValue(user.getGrade());
      }

      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      response.setHeader("Content-Disposition", "attachment; filename=students.xlsx");
      workbook.write(response.getOutputStream());
    }
  }
}

