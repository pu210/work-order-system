package com.eeit219.work_order_system.modules.a.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String from;

    public MailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    // 寄發重設密碼信件
    public void sendPasswordResetEmail(
            String recipient,
            String resetUrl) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("工單系統－重設密碼");
        message.setText("""
                您好：

                我們收到您的密碼重設申請。

                請點擊以下連結設定新密碼：
                %s

                此連結將於 15 分鐘後失效，且只能使用一次。

                如果您沒有申請重設密碼，請忽略這封信。
                """.formatted(resetUrl));

        mailSender.send(message);
    }
}