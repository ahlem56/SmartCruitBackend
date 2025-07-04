package tn.esprit.examen.controllers;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import tn.esprit.examen.entities.ChatMessage;
import tn.esprit.examen.entities.ContactResponse;
import tn.esprit.examen.entities.User;
import tn.esprit.examen.repositories.ChatRepository;
import tn.esprit.examen.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@AllArgsConstructor
public class ChatController {

    private final ChatRepository messageRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        System.out.println("📩 Message received: " + message); // LOG THIS

        ChatMessage saved = messageRepo.save(message);

        // Send to recipient-specific topic
        messagingTemplate.convertAndSend(
                "/topic/messages/" + message.getRecipientId(),
                saved
        );
    }

    @GetMapping("/history/{user1}/{user2}")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable String user1, @PathVariable String user2) {
        return messageRepo.findChatBetweenUsers(user1, user2);
    }


    @GetMapping("/conversations/{userId}")
    @ResponseBody
    public List<ContactResponse> getConversationContacts(@PathVariable String userId) {
        List<Object[]> results = messageRepo.findConversationsForUser(userId);

        return results.stream().map(obj -> {
            String otherUserId = (String) obj[0];
            String lastMessage = (String) obj[2]; // index depends on query order
            java.sql.Timestamp timestamp = (java.sql.Timestamp) obj[1];
            LocalDateTime lastTimestamp = timestamp.toLocalDateTime();

            ContactResponse contact = new ContactResponse();
            contact.setUserId(otherUserId);
            contact.setLastMessage(lastMessage);
            contact.setLastTimestamp(lastTimestamp);
            User user = userRepository.findById(Long.parseLong(otherUserId)).orElse(null);
            contact.setName(user != null ? user.getFullName() : "Unknown");
            contact.setProfilePictureUrl(user != null ? user.getProfilePictureUrl() : null);


            return contact;
        }).toList();
    }

}
