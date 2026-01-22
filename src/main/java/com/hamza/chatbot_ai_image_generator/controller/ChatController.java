package main.java.com.hamza.chatbot_ai_image_generator.controller;

import main.java.com.hamza.chatbot_ai_image_generator.dto.ChatRequest;
import main.java.com.hamza.chatbot_ai_image_generator.dto.ChatResponse;
import main.java.com.hamza.chatbot_ai_image_generator.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@Tag(name = "Chat API", description = "API for managing chat messages with AI responses")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    
    @Autowired
    public ChatController(ChatService chatService) {
        super();
        this.chatService = chatService;
    }
    
    // public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, String> request,
    //                                                    @AuthenticationPrincipal UserDetails userDetails) {
    //     try {
    //         String message = request.get("message");
    //         String type = request.getOrDefault("type", "TEXT");
            
    //         if (message == null || message.trim().isEmpty()) {
    //             return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
    //         }
            
    //         // Get user
    //         User user = userRepository.findByUsername(userDetails.getUsername())
    //                 .orElseThrow(() -> new RuntimeException("User not found"));
            
    //         // Create chat message
    //         ChatMessage chatMessage = new ChatMessage(user, message, ChatMessage.MessageType.valueOf(type));
            
    //         long startTime = System.currentTimeMillis();
            
    //         // Process based on message type
    //         switch (ChatMessage.MessageType.valueOf(type)) {
    //             case TEXT:
    //                 String aiResponse = aiService.generateTextResponse(message);
    //                 chatMessage.setAiResponse(aiResponse);
    //                 break;
                    
    //             case IMAGE_GENERATION:
    //                 String imageUrl = imageGenerationService.generateImageFromDescription(message);
    //                 chatMessage.setImageUrl(imageUrl);
    //                 chatMessage.setAiResponse("Image generated successfully!");
    //                 break;
                    
    //             case PDF_GENERATION:
    //                 String pdfUrl = pdfGenerationService.generatePDFFromRequest(message);
    //                 chatMessage.setPdfUrl(pdfUrl);
    //                 chatMessage.setAiResponse("PDF generated successfully!");
    //                 break;
    //         }
            
    //         long endTime = System.currentTimeMillis();
    //         chatMessage.setResponseTimeMs(endTime - startTime);
            
    //         // Save message
    //         chatMessageRepository.save(chatMessage);
            
    //         // Prepare response
    //         Map<String, Object> response = new HashMap<>();
    //         response.put("id", chatMessage.getId());
    //         response.put("message", chatMessage.getMessageContent());
    //         response.put("aiResponse", chatMessage.getAiResponse());
    //         response.put("imageUrl", chatMessage.getImageUrl());
    //         response.put("pdfUrl", chatMessage.getPdfUrl());
    //         response.put("type", chatMessage.getMessageType());
    //         response.put("timestamp", chatMessage.getCreatedAt());
    //         response.put("responseTime", chatMessage.getResponseTimeMs());

    @PostMapping("/message")
    @Operation(summary = "Send a chat message", description = "Process a chat message and return AI response")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        logger.info("Received message from user: {}", userDetails.getUsername());
        
        ChatResponse response = chatService.processMessage(request, userDetails.getUsername());
        
        return ResponseEntity.ok(response);
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
    
     @DeleteMapping("/clear-old")
    @Operation(summary = "Clear chat history", description = "Delete all chat messages for the authenticated user")
  
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

    @DeleteMapping("/clear")
    @Operation(summary = "Clear chat history", description = "Delete all chat messages for the authenticated user")
    public ResponseEntity<Map<String, String>> clearChatHistory(
            @Parameter(hidden = true) Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        chatService.clearChatHistory(userDetails.getUsername());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Chat history cleared successfully");
        
        return ResponseEntity.ok(response);
    }
}
