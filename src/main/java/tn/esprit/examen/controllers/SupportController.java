package tn.esprit.examen.controllers;

import jakarta.servlet.annotation.MultipartConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.examen.services.EmailService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support")
@MultipartConfig
public class SupportController {

    private final EmailService emailService;

    @PostMapping("/send")
    public void sendSupport(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment
    ) {
        emailService.sendSupportRequest(fullName, email, subject, message, attachment);
    }
}
