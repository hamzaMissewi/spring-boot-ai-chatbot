package main.java.com.hamza.chatbot_ai_image_generator.service;

import main.java.com.hamza.chatbot_ai_image_generator.dto.ChatRequest;
import main.java.com.hamza.chatbot_ai_image_generator.dto.ChatResponse;
import main.java.com.hamza.chatbot_ai_image_generator.entity.ChatMessage;
import main.java.com.hamza.chatbot_ai_image_generator.entity.User;
import main.java.com.hamza.chatbot_ai_image_generator.exception.BusinessException;
import main.java.com.hamza.chatbot_ai_image_generator.exception.ResourceNotFoundException;
import main.java.com.hamza.chatbot_ai_image_generator.repository.ChatMessageRepository;
import main.java.com.hamza.chatbot_ai_image_generator.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AIService aiService;
    private final ImageGenerationService imageGenerationService;
    private final PDFGenerationService pdfGenerationService;
    
    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository,
                      UserRepository userRepository,
                      AIService aiService,
                      ImageGenerationService imageGenerationService,
                      PDFGenerationService pdfGenerationService) {
        super();
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.imageGenerationService = imageGenerationService;
        this.pdfGenerationService = pdfGenerationService;
    }
    
    public ChatResponse processMessage(ChatRequest request, String username) {
        logger.info("Processing {} message for user: {}", request.getMessageType(), username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        long startTime = System.currentTimeMillis();
        String messageId = UUID.randomUUID().toString();
        
        try {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setUser(user);
            chatMessage.setContent(request.getMessage());
            chatMessage.setMessageType(ChatMessage.MessageType.valueOf(request.getMessageType()));
            chatMessage.setCreatedAt(LocalDateTime.now());
            
            String response = "";
            String imageUrl = null;
            String pdfUrl = null;
            
            switch (request.getMessageType()) {
                case "TEXT":
                    response = aiService.generateTextResponse(request.getMessage());
                    break;
                    
                case "IMAGE_GENERATION":
                    imageUrl = imageGenerationService.generateImageFromDescription(request.getMessage());
                    response = "Image generated successfully! You can view it at: " + imageUrl;
                    break;
                    
                case "PDF_GENERATION":
                    pdfUrl = pdfGenerationService.generatePDFFromRequest(request.getMessage());
                    response = "PDF generated successfully! You can download it at: " + pdfUrl;
                    break;
                    
                default:
                    throw new BusinessException("Invalid message type: " + request.getMessageType());
            }
            
            chatMessage.setAiResponse(response);
            chatMessage.setImageUrl(imageUrl);
            chatMessage.setPdfUrl(pdfUrl);
            
            long endTime = System.currentTimeMillis();
            chatMessage.setResponseTime(endTime - startTime);
            
            ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
            
            logger.info("Message processed successfully in {}ms", chatMessage.getResponseTime());
            
            return new ChatResponse(
                    savedMessage.getId().toString(),
                    savedMessage.getContent(),
                    savedMessage.getMessageType().toString(),
                    savedMessage.getAiResponse(),
                    savedMessage.getImageUrl(),
                    savedMessage.getPdfUrl(),
                    savedMessage.getCreatedAt(),
                    savedMessage.getResponseTime()
            );
            
        } catch (Exception e) {
            logger.error("Error processing message", e);
            throw new BusinessException("Failed to process message: " + e.getMessage(), e);
        }
    }
    
    public List<ChatResponse> getChatHistory(String username, Pageable pageable) {
        logger.info("Retrieving chat history for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        Page<ChatMessage> messages = chatMessageRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        return messages.stream()
                .map(this::convertToChatResponse)
                .collect(Collectors.toList());
    }
    
    public void clearChatHistory(String username) {
        logger.info("Clearing chat history for user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        chatMessageRepository.deleteByUser(user);
        logger.info("Chat history cleared for user: {}", username);
    }
    
    private ChatResponse convertToChatResponse(ChatMessage message) {
        return new ChatResponse(
                message.getId().toString(),
                message.getContent(),
                message.getMessageType().toString(),
                message.getAiResponse(),
                message.getImageUrl(),
                message.getPdfUrl(),
                message.getCreatedAt(),
                message.getResponseTime()
        );
    }
}
