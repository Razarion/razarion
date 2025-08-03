package com.btxtech.server.user;

import com.btxtech.server.model.UserEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class RegisterServiceIT {

    @Autowired
    private RegisterService registerService;

    @Test
    public void startEmailVerifyingProcess() throws Exception {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@localhost");

        registerService.startEmailVerifyingProcess(userEntity);
        assertThat(userEntity.getVerificationId()).isNotNull();
        assertThat(userEntity.getVerificationId()).isNotNull();

        Thread.sleep(1000);

        MimeMessage[] messages = fetchMessages();
        assertThat(messages).hasSizeGreaterThanOrEqualTo(1);

        MimeMessage message = messages[0];

        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("test@localhost");
        assertThat(message.getSubject()).contains("verify");
        assertThat(extractTextFromMessage(message)).contains("https://www.razarion.com/verify-email/" + userEntity.getVerificationId());
    }


    public MimeMessage[] fetchMessages() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8025/api/v2/messages"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode items = root.get("items");

        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage[] messages = new MimeMessage[items.size()];

        for (int i = 0; i < items.size(); i++) {
            String raw = items.get(i).get("Raw").get("Data").asText();
            byte[] decoded = raw.getBytes();
            messages[i] = new MimeMessage(session, new ByteArrayInputStream(decoded));
        }

        return messages;
    }

    private String extractTextFromMessage(Message message) throws Exception {
        Object content = message.getContent();

        if (content instanceof String str) {
            return str;
        } else if (content instanceof MimeMultipart multipart) {
            return extractTextFromMultipart(multipart);
        } else {
            throw new IllegalStateException("Unsupported content type: " + content.getClass());
        }
    }

    private String extractTextFromMultipart(MimeMultipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (part.isMimeType("text/plain")) {
                return (String) part.getContent();
            } else if (part.isMimeType("text/html")) {
                return (String) part.getContent();
            } else if (part.getContent() instanceof MimeMultipart nested) {
                String result = extractTextFromMultipart(nested);
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return "";
    }

}
