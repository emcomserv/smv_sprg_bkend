package com.smartvehicle.service;

import com.smartvehicle.entity.EmailSettings;
import com.smartvehicle.repository.EmailSettingsRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    @Autowired
    private EmailSettingsRepository emailSettingsRepository;

    private JavaMailSenderImpl buildSender(EmailSettings settings) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(settings.getHost());
        sender.setPort(settings.getPort());
        sender.setUsername(settings.getUsername());
        sender.setPassword(settings.getPassword());
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        return sender;
    }

    public EmailSettings getActiveSettings() {
        List<EmailSettings> all = emailSettingsRepository.findAll();
        if (all.isEmpty()) {
            throw new IllegalStateException("Email settings not configured");
        }
        return all.get(0);
    }

    public void sendEmail(String to, String subject, String htmlBody) throws MessagingException {
        EmailSettings settings = getActiveSettings();
        JavaMailSenderImpl sender = buildSender(settings);
        var message = sender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true);
        helper.setFrom(settings.getUsername());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        sender.send(message);
    }
}


