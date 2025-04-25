package tn.esprit.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${mailing.frontend.activation-url:http://localhost:4200/activate_account}")
    private String frontendActivationUrl;

    @Value("${mailing.frontend.reset-url:http://localhost:4200/reset-password}")
    private String frontendResetUrl;

    @Async
    public void sendActivationEmail(String to, String username, String activationCode)
            throws MessagingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            Map<String, Object> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("activationCode", activationCode);
            variables.put("activationLink", frontendActivationUrl);

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(
                    "activate_account",
                    context
            );

            helper.setTo(to);
            helper.setSubject("Your Account Activation Code");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Activation email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send activation email to: {}", to, e);
            throw e;
        }
    }

    @Async
    public void sendAdminNotification(String to, String subject, String content) {
        try {
            // For admin, we'll use a simple text email
            // You could also create a HTML template for admin notifications
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("Admin notification sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send admin notification: {}", e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) throws MessagingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String resetLink = frontendResetUrl + "?token=" + resetToken;

            Map<String, Object> variables = new HashMap<>();
            variables.put("firstName", name);
            variables.put("resetLink", resetLink);
            variables.put("resetToken", resetToken);  // Pass just the token separately

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("reset_password", context);

            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email", e);
            throw e;
        }
    }
}