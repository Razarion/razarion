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
    public void sendProgressLink(String email, String link, ProgressSaveService.LinkMail kind) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(NO_REPLY_EMAIL, PERSONAL_NAME));
            helper.setTo(email);
            helper.setSubject(switch (kind) {
                case SAVE -> "Save your Razarion progress";
                case SIGN_IN -> "Continue your Razarion game";
                case ACCOUNT -> "Sign in to your Razarion account";
            });

            Map<String, Object> model = new HashMap<>();
            model.put("greeting", "Hello,");
            model.put("main", switch (kind) {
                case SAVE -> "Open the link below to save your progress. Afterwards you can continue on any device with this address:";
                case SIGN_IN -> "Open the link below to continue your saved game on this device:";
                case ACCOUNT -> "This address belongs to your Razarion account. Open the link below to sign in to it on this device - no password needed:";
            });
            model.put("link", link);
            String validity = "The link works once and for " + ProgressSaveService.LINK_VALID_MINUTES
                    + " minutes. If you did not ask for it, just ignore this mail.";
            // A password account's login is its address, so that one is kept - the other two are not.
            model.put("closing", kind == ProgressSaveService.LinkMail.ACCOUNT
                    ? validity
                    : validity + " We do not keep your address and will not write to you.");
            model.put("razarionTeam", "The Razarion Team");

            Template template = freemarkerConfig.getTemplate("registration-confirmation.ftl");
            helper.setText(plainText(model), FreeMarkerTemplateUtils.processTemplateIntoString(template, model));
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

            helper.setText(plainText(model), html);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * The same mail as plain text, sent alongside the HTML. A mail that is HTML only scores as spam
     * in the common filters (SpamAssassin MIME_HTML_ONLY), and "save your progress" is a mail the
     * player is waiting for: of the first nine who asked for one on PROD, four never opened the link
     * (2026-11-17..22).
     */
    static String plainText(Map<String, Object> model) {
        return model.get("greeting") + "\n\n"
                + model.get("main") + "\n\n"
                + model.get("link") + "\n\n"
                + model.get("closing") + "\n\n"
                + model.get("razarionTeam") + "\n";
    }
}
