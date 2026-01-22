package main.java.com.hamza.chatbot_ai_image_generator.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIService {
    
    private final ChatClient chatClient;
    
    @Value("${spring.ai.google.genai.chat.options.model:gemini-pro}")
    private String model;
    
    @Value("${spring.ai.google.genai.chat.options.temperature:0.7}")
    private Double temperature;
    
    @Autowired
    public AIService(ChatClient.Builder chatClientBuilder) {
        super();
        this.chatClient = chatClientBuilder.build();
    }
    
    public String generateTextResponse(String userMessage) {
        try {
            String promptTemplate = """
                You are a helpful and friendly AI assistant. Please respond to the following user message in a natural, conversational manner.
                
                User message: {message}
                
                Please provide a helpful and accurate response.
                """;
            
            PromptTemplate template = new PromptTemplate(promptTemplate, Map.of("message", userMessage));
            Prompt prompt = template.create();
            
            return chatClient.prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AI response: " + e.getMessage(), e);
        }
    }
    
    public String generateImagePrompt(String userDescription) {
        try {
            String promptTemplate = """
                You are an AI image generation specialist. Convert the following user description into a detailed, professional image generation prompt.
                
                User description: {description}
                
                Create a detailed prompt that includes:
                - Subject matter
                - Style (photorealistic, artistic, cartoon, etc.)
                - Lighting and atmosphere
                - Composition and framing
                - Color scheme
                - Any additional details that would enhance the image
                
                Return only the image generation prompt, no additional text.
                """;
            
            PromptTemplate template = new PromptTemplate(promptTemplate, Map.of("description", userDescription));
            Prompt prompt = template.create();
            
            return chatClient.prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate image prompt: " + e.getMessage(), e);
        }
    }
    
    public String generatePdfContent(String userRequest) {
        try {
            String promptTemplate = """
                You are creating content for a PDF document. Based on the user's request, generate well-structured content that would be suitable for a PDF.
                
                User request: {request}
                
                Please create content that includes:
                - A clear title
                - Well-organized sections with headings
                - Detailed information relevant to the request
                - Professional formatting suggestions
                
                Format the content with clear section markers like [TITLE], [SECTION], [SUBSECTION] that can be used for PDF generation.
                """;
            
            PromptTemplate template = new PromptTemplate(promptTemplate, Map.of("request", userRequest));
            Prompt prompt = template.create();
            
            return chatClient.prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF content: " + e.getMessage(), e);
        }
    }
}
