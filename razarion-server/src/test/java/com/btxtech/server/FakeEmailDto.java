package com.btxtech.server;

/**
 * Created by Beat
 * on 08.02.2018.
 */
public class FakeEmailDto {
    private String envelopeSender;
    private String envelopeReceiver;
    private String subject;
    private String contentType;
    private String content;

    public String getEnvelopeSender() {
        return envelopeSender;
    }

    public FakeEmailDto setEnvelopeSender(String envelopeSender) {
        this.envelopeSender = envelopeSender;
        return this;
    }

    public String getEnvelopeReceiver() {
        return envelopeReceiver;
    }

    public FakeEmailDto setEnvelopeReceiver(String envelopeReceiver) {
        this.envelopeReceiver = envelopeReceiver;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public FakeEmailDto setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public FakeEmailDto setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContent() {
        return content;
    }

    public FakeEmailDto setContent(String content) {
        this.content = content;
        return this;
    }
}
