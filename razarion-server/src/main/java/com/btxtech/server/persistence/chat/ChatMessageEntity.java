package com.btxtech.server.persistence.chat;

import com.btxtech.server.user.UserEntity;
import com.btxtech.shared.datatypes.ChatMessage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Beat
 * on 29.12.2017.
 */
@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private UserEntity userEntity;
    @Lob
    @Column(length = 500)
    private String message;
    @Column(columnDefinition = "DATETIME(3)")
    private Date timestamp;
    @Column(length = 190)
// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String sessionId;


    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ChatMessage toChatMessage() {
        return new ChatMessage().setUserId(userEntity.getId()).setUserName(userEntity.getName()).setMessage(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChatMessageEntity that = (ChatMessageEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
