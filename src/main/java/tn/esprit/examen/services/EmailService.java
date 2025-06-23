package tn.esprit.examen.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetLink(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String htmlContent = "<p>Bonjour,</p>" +
                    "<p>Voici le lien pour réinitialiser votre mot de passe :</p>" +
                    "<p><a href=\"" + resetLink + "\">Réinitialiser le mot de passe</a></p>" +
                    "<p>Ce lien expirera dans 30 minutes.</p>";

            helper.setText(htmlContent, true);
            helper.setTo(toEmail);
            helper.setSubject("Réinitialisation du mot de passe - SmartCruit");
            helper.setFrom("bouchahouaahlem@gmail.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'e-mail", e);
        }
    }
}
