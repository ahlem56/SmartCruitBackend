package tn.esprit.examen.controllers;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.entities.ChatMessage;
import tn.esprit.examen.entities.ContactResponse;
import tn.esprit.examen.entities.User;
import tn.esprit.examen.repositories.ChatRepository;
import tn.esprit.examen.repositories.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class ChatController {

    private final ChatRepository messageRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;


    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage message) {
        try {
            message.setTimestamp(LocalDateTime.now());

            System.out.println("📩 WebSocket message received:");
            System.out.println("    Sender: " + message.getSenderId());
            System.out.println("    Recipient: " + message.getRecipientId());
            System.out.println("    Type: " + message.getType());
            System.out.println("    Content length: " + (message.getContent() != null ? message.getContent().length() : "null"));

            if (message.getType() == null || message.getType().isBlank()) {
                message.setType("text");
            }

            ChatMessage saved = messageRepo.save(message);

            messagingTemplate.convertAndSend(
                    "/topic/messages/" + message.getRecipientId(),
                    saved
            );
        } catch (Exception e) {
            System.err.println("❌ Failed to save message:");
            e.printStackTrace();
        }
    }


    @GetMapping("/history/{user1}/{user2}")
    @ResponseBody
    public List<Map<String, Object>> getChatHistory(@PathVariable String user1, @PathVariable String user2) {
        List<ChatMessage> messages = messageRepo.findChatBetweenUsers(user1, user2);

        return messages.stream().map(msg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("senderId", msg.getSenderId());
            map.put("recipientId", msg.getRecipientId());
            map.put("content", msg.getContent());
            map.put("timestamp", msg.getTimestamp());
            map.put("type", msg.getType()); // ✅ Add type

            User sender = userRepository.findById(Long.parseLong(msg.getSenderId())).orElse(null);
            map.put("senderName", sender != null ? sender.getFullName() : "Unknown");

            return map;
        }).toList();
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

    @PostMapping("/upload")
    @ResponseBody
    public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        try {
            String uploadDir = "uploads/"; // Make sure this folder exists and is writable
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + filename);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            String fileUrl = "http://localhost:8089/SmartCruit/" + uploadDir + filename;
            response.put("url", fileUrl);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            response.put("error", "Upload failed");
            return response;
        }
    }


}
