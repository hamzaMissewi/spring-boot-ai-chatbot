package main.java.com.hamza.chatbot_ai_image_generator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageGenerationService {
    
    private final AIService aiService;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;
    
    @Autowired
    public ImageGenerationService(AIService aiService) {
        super();
        this.aiService = aiService;
    }
    
    public String generateImageFromDescription(String description) {
        try {
            // Generate a detailed prompt using AI
            String imagePrompt = aiService.generateImagePrompt(description);
            
            // For demo purposes, we'll create a placeholder image
            // In production, you would integrate with an actual image generation API
            // like DALL-E, Midjourney, or Stable Diffusion
            return createPlaceholderImage(imagePrompt);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate image: " + e.getMessage(), e);
        }
    }
    
    private String createPlaceholderImage(String prompt) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "images");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String fileName = "generated_" + UUID.randomUUID().toString() + ".png";
            Path filePath = uploadPath.resolve(fileName);
            
            // For demo purposes, we'll create a simple placeholder
            // In production, this would be replaced with actual image generation
            String placeholderText = "Generated Image\\nPrompt: " + prompt.substring(0, Math.min(50, prompt.length())) + "...";
            
            // Create a simple text-based placeholder (this is just for demo)
            // In production, you'd use an actual image generation API
            savePlaceholderImage(filePath, placeholderText);
            
            // Return the URL
            return baseUrl + "/api/images/" + fileName;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }
    
    private void savePlaceholderImage(Path filePath, String text) throws IOException {
        // This is a placeholder implementation
        // In production, you would:
        // 1. Call an image generation API (DALL-E, Midjourney, Stable Diffusion, etc.)
        // 2. Download the generated image
        // 3. Save it to the file system
        
        // For now, we'll create a simple text file as a placeholder
        String content = "PLACEHOLDER IMAGE\\n\\nGenerated for prompt:\\n" + text + "\\n\\n" +
                        "Note: This is a demo placeholder. In production, this would be an actual image.";
        Files.write(filePath, content.getBytes());
    }
    
    public String uploadImage(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String fileName = "upload_" + UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return the URL
            return baseUrl + "/api/uploads/" + fileName;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }
    
    public byte[] getImageFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, "images", fileName);
            if (!Files.exists(filePath)) {
                filePath = Paths.get(uploadDir, "uploads", fileName);
            }
            
            if (!Files.exists(filePath)) {
                throw new RuntimeException("Image file not found: " + fileName);
            }
            
            return Files.readAllBytes(filePath);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image file: " + e.getMessage(), e);
        }
    }
}
