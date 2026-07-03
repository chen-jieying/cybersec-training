package com.example.cybersec.controller;

import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {

  @GetMapping("/pdf")
  public void getPdf(HttpServletResponse response) throws IOException {
    String pdfContent = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Count 1/MediaBox[0 0 612 792]/Kids[3 0 R]>>endobj\n3 0 obj<</Type/Page/Parent 2 0 R/Resources<</Font<</F1 4 0 R>>>>/Contents 5 0 R>>endobj\n4 0 obj<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>endobj\n5 0 obj<</Length 44>>stream\nBT /F1 24 Tf 72 720 Td (网络安全教学资源) Tj ET\nendstream endobj\nxref\n0 6\n0000000000 65535 f \n0000000010 00000 n \n0000000054 00000 n \n0000000102 00000 n \n0000000191 00000 n \n0000000256 00000 n \ntrailer<</Size 6/Root 1 0 R>>\nstartxref\n340\n%%EOF";
    response.setContentType(MediaType.APPLICATION_PDF_VALUE);
    response.getOutputStream().write(pdfContent.getBytes());
    response.getOutputStream().flush();
  }
}

