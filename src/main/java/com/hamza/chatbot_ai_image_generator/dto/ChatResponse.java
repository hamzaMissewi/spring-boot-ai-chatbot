package main.java.com.hamza.chatbot_ai_image_generator.dto;

import java.time.LocalDateTime;

public class ChatResponse {
    
    private String id;
    private String message;
    private String messageType;
    private String response;
    private String imageUrl;
    private String pdfUrl;
    private LocalDateTime timestamp;
    private Long responseTime;
    
    public ChatResponse() {
        super();
    }
    
    public ChatResponse(String id, String message, String messageType, String response, 
                       String imageUrl, String pdfUrl, LocalDateTime timestamp, Long responseTime) {
        super();
        this.id = id;
        this.message = message;
        this.messageType = messageType;
        this.response = response;
        this.imageUrl = imageUrl;
        this.pdfUrl = pdfUrl;
        this.timestamp = timestamp;
        this.responseTime = responseTime;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Long getResponseTime() { return responseTime; }
    public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
}
