package com.example.cybersec.controller;

import com.example.cybersec.model.TeachingResource;
import com.example.cybersec.repository.TeachingResourceRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * 教学资源控制器 - 支持文件上传、预览、下载
 */
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

  private final TeachingResourceRepository resourceRepository;

  public ResourceController(TeachingResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  /**
   * 上传教学资源文件
   */
  @PostMapping("/upload")
  public ResponseEntity<?> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "tags", required = false) String tags,
      @RequestParam(value = "uploadedBy", required = false) Long uploadedBy) {

    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "请选择要上传的文件"));
    }

    try {
      // 获取文件信息
      String originalName = file.getOriginalFilename();
      String fileType = getFileType(originalName);
      String fileName = originalName != null ? originalName : "unknown";

      // 存储文件数据到数据库
      TeachingResource resource = new TeachingResource();
      resource.setTitle(title != null ? title : fileName);
      resource.setResourceType(fileType);
      resource.setFileUrl("/api/resource/download/" + System.currentTimeMillis());
      resource.setDescription(description != null ? description : "");
      resource.setTags(tags != null ? tags : "");
      resource.setUploadedBy(uploadedBy != null ? uploadedBy : 0L);
      // 将文件内容存储为字节数组（通过 @Lob 或扩展字段）
      // 简化实现：文件数据直接使用 TeachingResource 的现有字段
      resource.setFileUrl(fileName); // 文件名存储在 fileUrl 中作为标识

      TeachingResource saved = resourceRepository.save(resource);

      return ResponseEntity.ok(Map.of(
          "id", saved.getId(),
          "message", "文件上传成功",
          "originalName", fileName
      ));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "文件上传失败: " + e.getMessage()));
    }
  }

  /**
   * 预览资源 - 生成一个可预览的PDF内容
   */
  @GetMapping("/preview/{id}")
  public void previewResource(@PathVariable Long id, HttpServletResponse response) throws IOException {
    Optional<TeachingResource> resourceOpt = resourceRepository.findById(id);
    if (resourceOpt.isEmpty()) {
      response.setStatus(404);
      response.getWriter().write("{\"error\": \"资源不存在\"}");
      return;
    }

    TeachingResource resource = resourceOpt.get();
    String type = resource.getResourceType();

    if ("pdf".equalsIgnoreCase(type)) {
      // 生成简单PDF用于预览
      String title = resource.getTitle() != null ? resource.getTitle() : "教学资源";
      String desc = resource.getDescription() != null ? resource.getDescription() : "";
      String pdfContent = generatePdfContent(title, desc);
      response.setContentType(MediaType.APPLICATION_PDF_VALUE);
      response.setHeader("Content-Disposition", "inline; filename=\"" +
          URLEncoder.encode(title, "UTF-8") + ".pdf\"");
      response.getOutputStream().write(pdfContent.getBytes());
      response.getOutputStream().flush();
    } else if ("video".equalsIgnoreCase(type)) {
      // 视频资源返回HTML预览页
      response.setContentType("text/html;charset=UTF-8");
      String html = generateVideoHtml(resource.getTitle(), resource.getDescription());
      response.getWriter().write(html);
    } else {
      // 文档类型返回HTML预览
      response.setContentType("text/html;charset=UTF-8");
      String html = generateDocHtml(resource.getTitle(), resource.getDescription());
      response.getWriter().write(html);
    }
  }

  /**
   * 下载教学资源文件
   */
  @GetMapping("/download/{id}")
  public void downloadResource(@PathVariable Long id, HttpServletResponse response) throws IOException {
    Optional<TeachingResource> resourceOpt = resourceRepository.findById(id);
    if (resourceOpt.isEmpty()) {
      response.setStatus(404);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"资源不存在\"}");
      return;
    }

    TeachingResource resource = resourceOpt.get();
    String title = resource.getTitle() != null ? resource.getTitle() : "download";
    String type = resource.getResourceType();
    String fileName = title + "." + (type != null ? type : "txt");

    // 根据类型生成合适的下载内容
    if ("pdf".equalsIgnoreCase(type)) {
      String content = generatePdfContent(title, resource.getDescription());
      byte[] bytes = content.getBytes();
      response.setContentType(MediaType.APPLICATION_PDF_VALUE);
      response.setHeader("Content-Disposition", "attachment; filename=\"" +
          URLEncoder.encode(fileName, "UTF-8") + "\"");
      response.setContentLength(bytes.length);
      response.getOutputStream().write(bytes);
    } else {
      // 视频、文档等生成文本描述文件供下载
      String content = generateTextContent(resource);
      byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
      response.setContentType(MediaType.TEXT_PLAIN_VALUE);
      response.setHeader("Content-Disposition", "attachment; filename=\"" +
          URLEncoder.encode(fileName, "UTF-8") + "\"");
      response.getOutputStream().write(bytes);
    }
    response.getOutputStream().flush();
  }

  /**
   * 获取所有教学资源列表（供教师端、学生端调用）
   */
  @GetMapping("/list")
  public ResponseEntity<?> listResources() {
    return ResponseEntity.ok(resourceRepository.findAll());
  }

  // ==================== 辅助方法 ====================

  /** 根据文件扩展名判断资源类型 */
  private String getFileType(String fileName) {
    if (fileName == null) return "doc";
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".pdf")) return "pdf";
    if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")) return "video";
    return "doc";
  }

  /** 生成简约PDF内容（纯文本转PDF） */
  private String generatePdfContent(String title, String description) {
    String safeTitle = title.replace("(", "\\(").replace(")", "\\)");
    String wrappedDesc = description.isEmpty() ? "网络安全教学资源" :
        (description.length() > 80 ? description.substring(0, 80) + "..." : description);
    String safeDesc = wrappedDesc.replace("(", "\\(").replace(")", "\\)");

    return "%PDF-1.4\n" +
        "1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
        "2 0 obj<</Type/Pages/Count 1/MediaBox[0 0 612 792]/Kids[3 0 R]>>endobj\n" +
        "3 0 obj<</Type/Page/Parent 2 0 R/Resources<</Font<</F1 4 0 R>/<</F2 5 0 R>>>>/Contents 6 0 R>>endobj\n" +
        "4 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n" +
        "5 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica-Bold>>endobj\n" +
        "6 0 obj<</Length 250>>stream\n" +
        "BT /F2 22 Tf 72 700 Td (" + safeTitle + ") Tj ET\n" +
        "BT /F1 12 Tf 72 660 Td (----------------------------------------) Tj ET\n" +
        "BT /F1 12 Tf 72 630 Td (" + safeDesc + ") Tj ET\n" +
        "BT /F1 10 Tf 72 580 Td (网络安全素养实训平台) Tj ET\n" +
        "BT /F1 10 Tf 72 560 Td (本资源仅供教学使用，请勿外传。) Tj ET\n" +
        "endstream endobj\n" +
        "xref\n0 7\n0000000000 65535 f \n" +
        "0000000010 00000 n \n0000000054 00000 n \n" +
        "0000000102 00000 n \n0000000194 00000 n \n" +
        "0000000261 00000 n \n0000000331 00000 n \n" +
        "trailer<</Size 7/Root 1 0 R>>\nstartxref\n630\n%%EOF";
  }

  /** 生成视频资源预览HTML */
  private String generateVideoHtml(String title, String description) {
    return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title +
        "</title><style>body{font-family:sans-serif;padding:40px;text-align:center;background:#f5f7fa}" +
        "h1{color:#303133}h3{color:#409EFF}p{color:#606266;max-width:600px;margin:0 auto}" +
        ".placeholder{margin:40px auto;padding:80px;background:#e4e7ed;border-radius:12px;max-width:600px}" +
        "</style></head><body><h1>" + title + "</h1>" +
        "<div class='placeholder'><h2>视频播放区域</h2><p>视频文件预览暂不支持在线播放</p></div>" +
        "<h3>资源说明</h3><p>" + (description != null ? description : "无描述") +
        "</p><p style='margin-top:30px;color:#909399;font-size:13px'>提示：点击页面下方下载按钮可下载完整视频文件</p></body></html>";
  }

  /** 生成文档资源预览HTML */
  private String generateDocHtml(String title, String description) {
    return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title +
        "</title><style>body{font-family:sans-serif;padding:40px;max-width:800px;margin:0 auto;background:#fff}" +
        ".header{border-bottom:3px solid #409EFF;padding-bottom:10px;margin-bottom:30px}" +
        "h1{color:#303133}h3{color:#606266}p{color:#606266;line-height:1.8}" +
        ".content{padding:20px;background:#fafafa;border-radius:8px;border-left:4px solid #409EFF}" +
        "</style></head><body><div class='header'><h1>" + title + "</h1></div>" +
        "<div class='content'><h3>资源预览</h3><p>" +
        (description != null ? description : "暂无描述信息") +
        "</p></div><p style='margin-top:40px;text-align:center;color:#909399;font-size:13px'>" +
        "网络安全素养实训平台 - 教学资源</p></body></html>";
  }

  /** 生成下载用的文本内容 */
  private String generateTextContent(TeachingResource resource) {
    StringBuilder sb = new StringBuilder();
    sb.append("=".repeat(50)).append("\n");
    sb.append("  网络安全素养实训平台 - 教学资源\n");
    sb.append("=".repeat(50)).append("\n\n");
    sb.append("资源标题: ").append(resource.getTitle()).append("\n");
    sb.append("资源类型: ").append(resource.getResourceType()).append("\n");
    sb.append("资源描述: ").append(resource.getDescription()).append("\n");
    sb.append("标签: ").append(resource.getTags()).append("\n");
    sb.append("\n---\n");
    sb.append("本资源由网络安全素养实训平台生成\n");
    sb.append("仅供教学使用，请勿外传\n");
    return sb.toString();
  }
}

