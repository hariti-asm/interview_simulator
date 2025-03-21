package ma.hariti.asmaa.wrm.simulator.service.serviceDefault;

import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceDefault implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public EmailServiceDefault(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendWelcomeEmail(String to, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Welcome to Intervo");
        message.setText("""
            Welcome to  Intervo!
            
            Your account has been created successfully.
            Your temporary password is: %s
            
            Please change your password after logging in for security purposes.
            
            Best regards,
            The Intervo Team
            """.formatted(password));

        try {
            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset Request - Intervo");
        message.setText("""
            Hello,
            
            We received a request to reset your password.
            To reset your password, use the following token: %s
            
            If you didn't request this, please ignore this email or contact support.
            This token will expire in 30 minutes.
            
            Best regards,
            The Intervo Team
            """.formatted(resetToken));

        try {
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }
}