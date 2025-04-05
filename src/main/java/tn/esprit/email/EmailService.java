package tn.esprit.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;  // Correct import
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.admin.email}")
    private String adminEmail;

    @Async
    public void sendEmail(
            String to,
            String username,
            String activationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {
        // Send to user
        sendEmailToAddress(to, username, activationUrl, activationCode, subject);

        // Send copy to admin
        sendEmailToAddress(adminEmail, username, activationUrl, activationCode,
                "[ADMIN COPY] " + subject);
    }

    private void sendEmailToAddress(
            String to,
            String username,
            String activationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        // Prepare template variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        variables.put("activation_code", activationCode);
        variables.put("confirmationUrl", activationUrl + "?token=" + activationCode);

        Context context = new Context();
        context.setVariables(variables);

        // Process Thymeleaf template
        String htmlContent = templateEngine.process("activate_account", context);

        // Configure email
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }
}