package main.java.com.hamza.chatbot_ai_image_generator.repository;

import com.hamza.chatbot_ai_image_generator.entity.ChatMessage;
import com.hamza.chatbot_ai_image_generator.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    Page<ChatMessage> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<ChatMessage> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user.id = :userId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findRecentMessagesByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.createdAt >= :startDate")
    long countMessagesCreatedAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT cm.messageType, COUNT(cm) FROM ChatMessage cm GROUP BY cm.messageType")
    List<Object[]> countMessagesByType();
    
    @Query("SELECT AVG(cm.responseTimeMs) FROM ChatMessage cm WHERE cm.responseTimeMs IS NOT NULL")
    Double getAverageResponseTime();
}
