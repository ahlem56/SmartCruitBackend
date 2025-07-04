package tn.esprit.examen.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.examen.entities.ChatMessage;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.senderId = :user1 AND m.recipientId = :user2) OR " +
            "(m.senderId = :user2 AND m.recipientId = :user1) " +
            "ORDER BY m.timestamp")
    List<ChatMessage> findChatBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

    @Query(value = """
    SELECT 
        CASE 
            WHEN c.sender_id = :userId THEN c.recipient_id 
            ELSE c.sender_id 
        END AS userId,
        MAX(c.timestamp) AS lastTimestamp,
        (
            SELECT content 
            FROM chat_message m 
            WHERE 
                (m.sender_id = c.sender_id AND m.recipient_id = c.recipient_id) OR 
                (m.sender_id = c.recipient_id AND m.recipient_id = c.sender_id)
            ORDER BY m.timestamp DESC 
            LIMIT 1
        ) AS lastMessage
    FROM chat_message c
    WHERE c.sender_id = :userId OR c.recipient_id = :userId
    GROUP BY userId
    ORDER BY lastTimestamp DESC
""", nativeQuery = true)
    List<Object[]> findConversationsForUser(@Param("userId") String userId);

}



