package main.java.com.hamza.chatbot_ai_image_generator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDTOs {
    
    public AuthDTOs() {
        super();
    }
    
    public static class RegisterRequest {
        
        public RegisterRequest() {
            super();
        }
        
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;
        
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        private String email;
        
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        private String password;
        
        // public RegisterRequest() {}
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class LoginRequest {
        
        public LoginRequest() {
            super();
        }
        
        @NotBlank(message = "Username cannot be blank")
        private String username;
        
        @NotBlank(message = "Password cannot be blank")
        private String password;
        
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class AuthResponse {
        
        private String token;
        private String type = "Bearer";
        private Long expiresIn;
        private UserInfo user;
        
        public AuthResponse() {
            super();
        }
        
        public AuthResponse(String token, Long expiresIn, UserInfo user) {
            super();
            this.token = token;
            this.expiresIn = expiresIn;
            this.user = user;
        }
        
        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
        
        public UserInfo getUser() { return user; }
        public void setUser(UserInfo user) { this.user = user; }
    }
    
    public static class UserInfo {
        
        private Long id;
        private String username;
        private String email;
        private String createdAt;
        
        public UserInfo() {
            super();
        }
        
        public UserInfo(Long id, String username, String email, String createdAt) {
            super();
            this.id = id;
            this.username = username;
            this.email = email;
            this.createdAt = createdAt;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
