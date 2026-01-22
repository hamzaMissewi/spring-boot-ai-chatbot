package main.java.com.hamza.chatbot_ai_image_generator.controller;

import com.hamza.chatbot_ai_image_generator.service.ImageGenerationService;
import com.hamza.chatbot_ai_image_generator.service.PDFGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileController {
    
    private final ImageGenerationService imageGenerationService;
    private final PDFGenerationService pdfGenerationService;
    
    @Autowired
    public FileController(ImageGenerationService imageGenerationService,
                       PDFGenerationService pdfGenerationService) {
                super();
        this.imageGenerationService = imageGenerationService;
        this.pdfGenerationService = pdfGenerationService;
    }
    
    @GetMapping("/images/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            byte[] imageBytes = imageGenerationService.getImageFile(fileName);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/pdfs/{fileName}")
    public ResponseEntity<Resource> getPDF(@PathVariable String fileName) {
        try {
            byte[] pdfBytes = pdfGenerationService.getPDFFIle(fileName);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please select a file to upload"));
            }
            
            String imageUrl = imageGenerationService.uploadImage(file);
            return ResponseEntity.ok(Map.of("url", imageUrl));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }
    
    @GetMapping("/uploads/{fileName}")
    public ResponseEntity<Resource> getUploadedImage(@PathVariable String fileName) {
        try {
            byte[] imageBytes = imageGenerationService.getImageFile(fileName);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
