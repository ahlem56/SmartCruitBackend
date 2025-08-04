package tn.esprit.examen.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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


    public void sendApplicationAcceptedEmail(String toEmail, String jobTitle, String companyName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String htmlContent = "<p>Dear Candidate,</p>" +
                    "<p>🎉 Congratulations! You’ve been <strong>accepted</strong> for the position of <strong>" + jobTitle + "</strong> at <strong>" + companyName + "</strong>.</p>" +
                    "<p>Please log in to your account to see the next steps or message your employer directly.</p>" +
                    "<br><p>Best regards,<br>SmartCruit Team</p>";

            helper.setText(htmlContent, true);
            helper.setTo(toEmail);
            helper.setSubject("🎉 You're Accepted! - SmartCruit");
            helper.setFrom("bouchahouaahlem@gmail.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send acceptance email", e);
        }
    }

    public void sendApplicationRejectedEmail(String toEmail, String jobTitle, String companyName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String htmlContent = "<p>Dear Candidate,</p>" +
                    "<p>We appreciate your interest in the <strong>" + jobTitle + "</strong> position at <strong>" + companyName + "</strong>.</p>" +
                    "<p>Unfortunately, we have decided to move forward with another candidate.</p>" +
                    "<p>We encourage you to apply to other opportunities in the future.</p>" +
                    "<br><p>Best regards,<br>SmartCruit Team</p>";

            helper.setText(htmlContent, true);
            helper.setTo(toEmail);
            helper.setSubject("📬 Application Update - SmartCruit");
            helper.setFrom("bouchahouaahlem@gmail.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send rejection email", e);
        }
    }


    public void sendSupportRequest(
            String fullName,
            String email,
            String subject,
            String message,
            MultipartFile attachment
    ) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, attachment != null, "utf-8");

            String htmlContent = "<p><strong>New Support Request Received</strong></p>" +
                    "<p><strong>From:</strong> " + fullName + " (" + email + ")</p>" +
                    "<p><strong>Subject:</strong> " + subject + "</p>" +
                    "<p><strong>Message:</strong></p>" +
                    "<p>" + message.replace("\n", "<br>") + "</p>";

            helper.setText(htmlContent, true);
            helper.setTo("bouchahouaahlem@gmail.com");
            helper.setSubject("📩 Support Request: " + subject);
            helper.setFrom("bouchahouaahlem@gmail.com");

            if (attachment != null && !attachment.isEmpty()) {
                helper.addAttachment(attachment.getOriginalFilename(), attachment);
            }

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send support request email", e);
        }
    }


}
