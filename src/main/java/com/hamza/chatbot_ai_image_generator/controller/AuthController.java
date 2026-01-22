package main.java.com.hamza.chatbot_ai_image_generator.controller;

import main.java.com.hamza.chatbot_ai_image_generator.entity.User;
import main.java.com.hamza.chatbot_ai_image_generator.repository.UserRepository;
import main.java.com.hamza.chatbot_ai_image_generator.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        super();
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Check if user already exists
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username already exists"));
            }
            
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email already exists"));
            }
            
            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            userRepository.save(user);
            
            // Generate JWT token
            String token = jwtService.generateToken(user.getUsername());
            
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "message", "User registered successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String token = jwtService.generateToken(request.getUsername());
            
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "message", "Login successful"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid credentials"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("createdAt", user.getCreatedAt());
        
        return ResponseEntity.ok(response);
    }
    
        // // DTO classes
        // public static class RegisterRequest {
        //     private String username;
        //     private String email;
        //     private String password;
            
        //     // Getters and setters
        //     public String getUsername() { return username; }
        //     public void setUsername(String username) { this.username = username; }
            
        //     public String getEmail() { return email; }
        //     public void setEmail(String email) { this.email = email; }
            
        //     public String getPassword() { return password; }
        //     public void setPassword(String password) { this.password = password; }
        // }
        
        // public static class LoginRequest {
        //     private String username;
        //     private String password;
            
        //     // Getters and setters
        //     public String getUsername() { return username; }
        //     public void setUsername(String username) { this.username = username; }
            
        //     public String getPassword() { return password; }
        //     public void setPassword(String password) { this.password = password; }
        // }
}
