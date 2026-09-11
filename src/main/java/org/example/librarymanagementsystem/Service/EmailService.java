package org.example.librarymanagementsystem.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String toEmail, String memberName) {
        try {
            Context context = new Context();
            context.setVariable("memberName", memberName);
            String htmlContent = templateEngine.process("welcome-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to the Library, " + memberName + "!");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    public void sendDailyReportEmail(String toEmail, long totalBooks, long activeMembers, long overdueCount) {
        try {
            Context context = new Context();
            context.setVariable("totalBooks", totalBooks);
            context.setVariable("activeMembers", activeMembers);
            context.setVariable("overdueCount", overdueCount);
            context.setVariable("reportDate", LocalDate.now().toString());

            String htmlContent = templateEngine.process("daily-report", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Daily Library Report - " + LocalDate.now());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Daily report email sent to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send daily report to {}: {}", toEmail, e.getMessage());
        }
    }
}