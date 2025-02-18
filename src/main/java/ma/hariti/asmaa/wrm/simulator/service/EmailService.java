package ma.hariti.asmaa.wrm.simulator.service;
public interface EmailService {
    void sendWelcomeEmail(String to, String password);
    void sendPasswordResetEmail(String to, String resetToken);
}
