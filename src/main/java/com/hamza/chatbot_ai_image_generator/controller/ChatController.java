package main.java.com.hamza.chatbot_ai_image_generator.controller;

import com.hamza.chatbot_ai_image_generator.entity.ChatMessage;
import com.hamza.chatbot_ai_image_generator.entity.User;
import com.hamza.chatbot_ai_image_generator.service.AIService;
import com.hamza.chatbot_ai_image_generator.service.ImageGenerationService;
import com.hamza.chatbot_ai_image_generator.service.PDFGenerationService;
import com.hamza.chatbot_ai_image_generator.repository.ChatMessageRepository;
import com.hamza.chatbot_ai_image_generator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final AIService aiService;
    private final ImageGenerationService imageGenerationService;
    private final PDFGenerationService pdfGenerationService;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public ChatController(AIService aiService, 
                        ImageGenerationService imageGenerationService,
                        PDFGenerationService pdfGenerationService,
                        ChatMessageRepository chatMessageRepository,
                        UserRepository userRepository) {
        super();
        this.aiService = aiService;
        this.imageGenerationService = imageGenerationService;
        this.pdfGenerationService = pdfGenerationService;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }
    
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, String> request,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String message = request.get("message");
            String type = request.getOrDefault("type", "TEXT");
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
            }
            
            // Get user
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Create chat message
            ChatMessage chatMessage = new ChatMessage(user, message, ChatMessage.MessageType.valueOf(type));
            
            long startTime = System.currentTimeMillis();
            
            // Process based on message type
            switch (ChatMessage.MessageType.valueOf(type)) {
                case TEXT:
                    String aiResponse = aiService.generateTextResponse(message);
                    chatMessage.setAiResponse(aiResponse);
                    break;
                    
                case IMAGE_GENERATION:
                    String imageUrl = imageGenerationService.generateImageFromDescription(message);
                    chatMessage.setImageUrl(imageUrl);
                    chatMessage.setAiResponse("Image generated successfully!");
                    break;
                    
                case PDF_GENERATION:
                    String pdfUrl = pdfGenerationService.generatePDFFromRequest(message);
                    chatMessage.setPdfUrl(pdfUrl);
                    chatMessage.setAiResponse("PDF generated successfully!");
                    break;
            }
            
            long endTime = System.currentTimeMillis();
            chatMessage.setResponseTimeMs(endTime - startTime);
            
            // Save message
            chatMessageRepository.save(chatMessage);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("id", chatMessage.getId());
            response.put("message", chatMessage.getMessageContent());
            response.put("aiResponse", chatMessage.getAiResponse());
            response.put("imageUrl", chatMessage.getImageUrl());
            response.put("pdfUrl", chatMessage.getPdfUrl());
            response.put("type", chatMessage.getMessageType());
            response.put("timestamp", chatMessage.getCreatedAt());
            response.put("responseTime", chatMessage.getResponseTimeMs());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to process message: " + e.getMessage()));
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getChatHistory(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Pageable pageable = PageRequest.of(page, size);
            Page<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(user, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages.getContent());
            response.put("currentPage", messages.getNumber());
            response.put("totalItems", messages.getTotalElements());
            response.put("totalPages", messages.getTotalPages());
            response.put("hasNext", messages.hasNext());
            response.put("hasPrevious", messages.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch chat history: " + e.getMessage()));
        }
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<ChatMessage>> getRecentMessages(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestParam(defaultValue = "10") int limit) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Pageable pageable = PageRequest.of(0, limit);
            List<ChatMessage> messages = chatMessageRepository.findRecentMessagesByUserId(user.getId(), pageable);
            
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearChatHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(user);
            chatMessageRepository.deleteAll(messages);
            
            return ResponseEntity.ok(Map.of("message", "Chat history cleared successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to clear chat history: " + e.getMessage()));
        }
    }
}
