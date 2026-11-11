package com.btxtech.server.model;

import com.btxtech.shared.datatypes.ChatMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String userName;
    @Lob
    @Column(length = 500)
    private String message;
    @Column(columnDefinition = "DATETIME(3)")
    private Date timestamp;
    @Column(length = 300)
    private String instagramUrl;

    public ChatMessageEntity init(String userId, String userName, String message) {
        this.userId = userId;
        this.userName = userName;
        this.message = message;
        this.timestamp = new Date();
        return this;
    }

    public ChatMessageEntity instagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
        return this;
    }

    /** A reel on our Instagram the message points to, or null. */
    public String getInstagramUrl() {
        return instagramUrl;
    }

    public Long getId() {
        return id;
    }

    /** Null for an answer of the chat assistant: it is nobody's account. */
    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public ChatMessage toChatMessage() {
        return new ChatMessage().userName(userName).message(message).instagramUrl(instagramUrl)
                .timestamp(timestamp != null ? timestamp.getTime() : null);
    }
}
