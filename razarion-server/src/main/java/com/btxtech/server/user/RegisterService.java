package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.btxtech.shared.CommonUrl;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterService {

    public static final String NO_REPLY_EMAIL = "no-reply@razarion.com";
    public static final String PERSONAL_NAME = "Razarion";

    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfig;

    @Autowired
    public RegisterService(JavaMailSender mailSender, Configuration freemarkerConfig) {
        this.mailSender = mailSender;
        this.freemarkerConfig = freemarkerConfig;
    }

    public void startEmailVerifyingProcess(UserEntity userEntity) {
        userEntity.startVerification();
        sendEmailVerifyEmail(userEntity);
    }

    public void sendEmailVerifyEmail(UserEntity userEntity) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(new InternetAddress(NO_REPLY_EMAIL, PERSONAL_NAME));
            helper.setTo(userEntity.getEmail());

            helper.setSubject("Please verify your Razarion account");

            Map<String, Object> model = new HashMap<>();
            model.put("greeting", "Hello,");
            model.put("main", "Please confirm your email address by clicking the link below:");
            model.put("link", CommonUrl.generateVerificationLink(userEntity.getVerificationId()));
            model.put("closing", "Thank you for registering!");
            model.put("razarionTeam", "The Razarion Team");

            Template template = freemarkerConfig.getTemplate("registration-confirmation.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
