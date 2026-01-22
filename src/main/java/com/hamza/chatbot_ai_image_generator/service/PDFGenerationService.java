package main.java.com.hamza.chatbot_ai_image_generator.service;

import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PDFGenerationService {
    
    private final AIService aiService;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;
    
    @Autowired
    public PDFGenerationService(AIService aiService) {
        super();
        this.aiService = aiService;
    }
    
    public String generatePDFFromRequest(String userRequest) {
        try {
            // Generate content using AI
            String aiContent = aiService.generatePdfContent(userRequest);
            
            // Create PDF
            byte[] pdfBytes = createPDF(aiContent, userRequest);
            
            // Save PDF and return URL
            return savePDFAndGetUrl(pdfBytes);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
    
    private byte[] createPDF(String content, String originalRequest) {
        try {
            // Parse the AI content and convert to HTML
            String htmlContent = convertToHTML(content, originalRequest);
            
            // Convert HTML to PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, outputStream);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PDF: " + e.getMessage(), e);
        }
    }
    
    private String convertToHTML(String content, String originalRequest) {
        // Parse the content and create HTML
        String title = extractTitle(content);
        String body = formatContent(content);
        
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>" + escapeHtml(title) + "</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }\n" +
                "        h1 { color: #333; border-bottom: 2px solid #333; padding-bottom: 10px; }\n" +
                "        h2 { color: #555; margin-top: 30px; }\n" +
                "        h3 { color: #777; margin-top: 20px; }\n" +
                "        .metadata { background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n" +
                "        .section { margin-bottom: 25px; }\n" +
                "        .request { font-style: italic; color: #666; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"metadata\">\n" +
                "        <p><strong>Generated on:</strong> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>\n" +
                "        <p><strong>Original Request:</strong> <span class=\"request\">" + escapeHtml(originalRequest) + "</span></p>\n" +
                "    </div>\n" +
                "    <h1>" + escapeHtml(title) + "</h1>\n" +
                "    <div class=\"content\">\n" +
                "        " + body + "\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
    
    private String extractTitle(String content) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith("[TITLE]")) {
                return line.substring("[TITLE]".length()).trim();
            }
        }
        return "Generated Document";
    }
    
    private String formatContent(String content) {
        String[] lines = content.split("\n");
        StringBuilder html = new StringBuilder();
        boolean inSection = false;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.startsWith("[TITLE]")) {
                continue; // Skip title, already handled
            } else if (line.startsWith("[SECTION]")) {
                if (inSection) {
                    html.append("</div>\n");
                }
                html.append("<div class=\"section\">\n");
                html.append("<h2>").append(escapeHtml(line.substring("[SECTION]".length()).trim())).append("</h2>\n");
                inSection = true;
            } else if (line.startsWith("[SUBSECTION]")) {
                html.append("<h3>").append(escapeHtml(line.substring("[SUBSECTION]".length()).trim())).append("</h3>\n");
            } else if (!line.isEmpty() && !line.startsWith("[")) {
                html.append("<p>").append(escapeHtml(line)).append("</p>\n");
            }
        }
        
        if (inSection) {
            html.append("</div>\n");
        }
        
        return html.toString();
    }
    
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    private String savePDFAndGetUrl(byte[] pdfBytes) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, "pdfs");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String fileName = "generated_" + UUID.randomUUID().toString() + ".pdf";
        Path filePath = uploadPath.resolve(fileName);
        
        // Save PDF
        Files.write(filePath, pdfBytes);
        
        // Return the URL
        return baseUrl + "/api/pdfs/" + fileName;
    }
    
    public byte[] getPDFFIle(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, "pdfs", fileName);
            
            if (!Files.exists(filePath)) {
                throw new RuntimeException("PDF file not found: " + fileName);
            }
            
            return Files.readAllBytes(filePath);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF file: " + e.getMessage(), e);
        }
    }
}
