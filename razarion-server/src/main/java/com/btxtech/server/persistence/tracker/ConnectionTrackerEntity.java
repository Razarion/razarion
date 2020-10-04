package com.btxtech.server.persistence.tracker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Beat
 * on 01.01.2018.
 */
// @Entity
// @Table(name = "TRACKER_CONNECTION")
public class ConnectionTrackerEntity {
    public enum Type {
        SYSTEM_OPEN,
        SYSTEM_CLOSE,
        GAME_OPEN,
        GAME_CLOSE
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition = "DATETIME(3)")
    private Date timeStamp;
    @Enumerated(EnumType.STRING)
    private Type type;
    @Column(length = 190) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String sessionId;
    private int humanPlayerId;


    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getHumanPlayerId() {
        return humanPlayerId;
    }

    public void setHumanPlayerId(int humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConnectionTrackerEntity that = (ConnectionTrackerEntity) o;

        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
