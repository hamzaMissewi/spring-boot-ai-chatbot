package main.java.com.hamza.chatbot_ai_image_generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {
    
    @NotBlank(message = "Message cannot be blank")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;
    
    @NotBlank(message = "Message type cannot be blank")
    private String messageType; // TEXT, IMAGE_GENERATION, PDF_GENERATION
    
    public ChatRequest() {
        super();
    }
    
    public ChatRequest(String message, String messageType) {
        super();
        this.message = message;
        this.messageType = messageType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
