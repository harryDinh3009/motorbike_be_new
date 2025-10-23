package com.translateai.config.mail;

import com.translateai.common.ApiStatus;
import com.translateai.config.exception.RestApiException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender javaMailSender;

    private final TemplateConfig templateConfig;

    @Value ("${spring.mail.username}")
    private String sender;

    @Async
    public void sendEmail(String[] toEmails, String subject, String titleEmail, String bodyEmail) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", titleEmail);
        variables.put("body", bodyEmail);

        String htmlBody = generateEmailContent(variables);
        sendSimpleMail(toEmails, htmlBody, subject);
    }

    private String generateEmailContent(Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateConfig.processTemplate("email", context);
    }

    private void sendSimpleMail(String[] recipients, String msgBody, String subject) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,
                    StandardCharsets.UTF_8.toString());
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setBcc(recipients);
            mimeMessageHelper.setText(msgBody, true);
            mimeMessageHelper.setSubject(subject);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RestApiException(ApiStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
