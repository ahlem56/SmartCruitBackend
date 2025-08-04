package tn.esprit.examen.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.examen.entities.Notification;
import tn.esprit.examen.entities.User;
import tn.esprit.examen.repositories.NotificationRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void notifyUser(User recipient, User sender, String message) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender); // ✅
        notification.setMessage(message);
        notificationRepository.save(notification);
    }


    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientUserIdOrderByTimestampDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Not found"));
        n.setRead(true); // ✅ use setRead now
        notificationRepository.save(n);
    }



}
