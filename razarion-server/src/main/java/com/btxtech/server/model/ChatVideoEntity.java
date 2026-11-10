package com.btxtech.server.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

/**
 * A short clip of a player's moment for the chat, filmed and made small by the recorder
 * (razarion-recorder): a player leaving the noob island. Only the newest few are kept
 * (ChatService.KEEP_VIDEOS); a chat message pointing at a removed one says so when played.
 */
@Entity
@Table(name = "CHAT_VIDEO")
public class ChatVideoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition = "DATETIME(3)")
    private Date timestamp;
    /** Whose moment it is. */
    private String userId;
    private int size;
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;

    public ChatVideoEntity init(String userId, byte[] data) {
        this.userId = userId;
        this.data = data;
        this.size = data.length;
        this.timestamp = new Date();
        return this;
    }

    public Integer getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}
