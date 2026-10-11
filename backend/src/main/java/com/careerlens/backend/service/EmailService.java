package com.careerlens.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${EMAIL_FROM:${spring.mail.username:}}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        String effectiveFrom = (fromEmail != null && !fromEmail.isBlank() && !fromEmail.contains("your-email@gmail.com"))
                ? fromEmail.trim()
                : "baklol5033@gmail.com";

        if (mailSender == null) {
            log.info("MailSender is null. OTP for {} is: {}", toEmail, otp);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(effectiveFrom, "CareerLens Verification");
            helper.setTo(toEmail.trim());
            helper.setSubject("CareerLens - Your 6-Digit Verification Code: " + otp);

            String htmlBody = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 24px; border: 1px solid #e4e8ef; border-radius: 12px; background-color: #ffffff;\">"
                    + "<div style=\"text-align: center; margin-bottom: 20px;\">"
                    + "<h2 style=\"color: #1a56db; margin: 0; font-size: 22px;\">CareerLens</h2>"
                    + "<p style=\"color: #64748b; font-size: 13px; margin-top: 4px;\">AI-Powered Job & Candidate Platform</p>"
                    + "</div>"
                    + "<p style=\"font-size: 14px; color: #334155; line-height: 1.5;\">Hello,</p>"
                    + "<p style=\"font-size: 14px; color: #334155; line-height: 1.5;\">Your 6-digit verification code for CareerLens is:</p>"
                    + "<div style=\"background-color: #ebf0ff; border: 1px solid #c7d7f9; text-align: center; font-size: 32px; font-weight: 800; letter-spacing: 8px; padding: 18px; margin: 24px 0; color: #1a56db; border-radius: 10px;\">"
                    + otp
                    + "</div>"
                    + "<p style=\"font-size: 13px; color: #64748b; line-height: 1.4;\">This code is valid for <strong>5 minutes</strong>. For security, do not share this code with anyone.</p>"
                    + "<hr style=\"border: none; border-top: 1px solid #e4e8ef; margin: 24px 0 16px 0;\">"
                    + "<p style=\"font-size: 11px; color: #94a3b8; text-align: center;\">&copy; 2026 CareerLens Platform. All rights reserved.</p>"
                    + "</div>";

            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Successfully sent OTP email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
            log.info("[FALLBACK] OTP for {} is: {}", toEmail, otp);
        }
    }
}
