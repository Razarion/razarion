package com.btxtech.server;

import org.subethamail.wiser.Wiser;

import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 08.02.2018.
 */
@Singleton
public class FakeEmailServer {
    private Wiser wiser;

    public void startFakeMailServer() {
        if (wiser != null) {
            throw new IllegalStateException("Fake email server is already running");
        }
        wiser = new Wiser(25);
        wiser.start();
    }

    public void stopFakeMailServer() {
        if (wiser == null) {
            throw new IllegalStateException("Fake email server is not running");
        }
        wiser.stop();
        wiser = null;
    }

    public List<FakeEmailDto> getMessagesAndClear() {
        if (wiser == null) {
            throw new IllegalStateException("Fake email server is not running");
        }

        List<FakeEmailDto> fakeEmailDtos = wiser.getMessages().stream().map(wiserMessage -> {
            try {
                FakeEmailDto fakeEmailDto = new FakeEmailDto().setEnvelopeReceiver(wiserMessage.getEnvelopeReceiver()).setEnvelopeSender(wiserMessage.getEnvelopeSender());
                MimeMessage mimeMessage = wiserMessage.getMimeMessage();
                fakeEmailDto.setSubject(mimeMessage.getSubject());
                fakeEmailDto.setContentType(mimeMessage.getContentType());
                fakeEmailDto.setContent((String) mimeMessage.getContent());
                return fakeEmailDto;
            } catch (MessagingException | IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        wiser.getMessages().clear();
        return fakeEmailDtos;
    }
}
