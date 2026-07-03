# 网络信息安全素养实训平台

一个面向初中生的网络信息安全素养实训平台，基于 Vue 3 + Element Plus 前端和 Spring Boot + MySQL 后端。

## 主要功能

- 学生 / 教师 / 管理员三种角色
- 互动式诈骗对话实训模块
- 基于 ECharts 的学情雷达图统计
- Excel 批量导入导出学生信息
- PDF 教学资源在线预览

## 运行方式

### 前端

```bash
cd frontend
npm install
npm run dev
```

### 后端

```bash
cd backend
mvn spring-boot:run
```

## 说明

后端示例使用内存登录与基础数据接口，可与 MySQL 数据库连接扩展。前端演示诈骗情景对话与多角色界面。
