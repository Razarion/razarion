package com.btxtech.server.user;

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

    /**
     * Mails out the verification link for a freshly minted verification id. Takes the plain values
     * instead of the {@link com.btxtech.server.model.UserEntity}: the mail is sent before the
     * registration is written, so there is nothing persistent to hand over yet - see
     * {@link com.btxtech.server.user.UserService#registerByEmail} for why that order matters.
     */
    /**
     * The mail of "save your progress": one link that either saves the player's progress under this
     * address or, when the address already holds a saved player, continues that player on the device
     * that opens it. The address is used for this one mail and is not kept anywhere.
     */
    public void sendProgressLink(String email, String link, boolean signIn) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(NO_REPLY_EMAIL, PERSONAL_NAME));
            helper.setTo(email);
            helper.setSubject(signIn ? "Continue your Razarion game" : "Save your Razarion progress");

            Map<String, Object> model = new HashMap<>();
            model.put("greeting", "Hello,");
            model.put("main", signIn
                    ? "Open the link below to continue your saved game on this device:"
                    : "Open the link below to save your progress. Afterwards you can continue on any device with this address:");
            model.put("link", link);
            model.put("closing", "The link works once and for " + ProgressSaveService.LINK_VALID_MINUTES
                    + " minutes. If you did not ask for it, just ignore this mail. We do not keep your address and will not write to you.");
            model.put("razarionTeam", "The Razarion Team");

            Template template = freemarkerConfig.getTemplate("registration-confirmation.ftl");
            helper.setText(FreeMarkerTemplateUtils.processTemplateIntoString(template, model), true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send the progress link", e);
        }
    }

    public void sendEmailVerifyEmail(String email, String verificationId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(new InternetAddress(NO_REPLY_EMAIL, PERSONAL_NAME));
            helper.setTo(email);

            helper.setSubject("Please verify your Razarion account");

            Map<String, Object> model = new HashMap<>();
            model.put("greeting", "Hello,");
            model.put("main", "Please confirm your email address by clicking the link below:");
            model.put("link", CommonUrl.generateVerificationLink(verificationId));
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
