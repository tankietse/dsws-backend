package com.webgis.dsws.controller.api.test;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class test {
    private final JavaMailSender mailSender;

    @GetMapping("/send-test-email")
    public String sendTestEmail() {
        System.out.println("Hello");
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            System.out.println("Đang gửi...");
            helper.setTo("vokiet888@gmail.com"); // Thay bằng email của bạn
            helper.setSubject("Test Email");
            helper.setText("This is a test email from AlertService.", false);

            mailSender.send(message);
            return "Test email sent successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send test email: " + e.getMessage();
        }
    }
}
