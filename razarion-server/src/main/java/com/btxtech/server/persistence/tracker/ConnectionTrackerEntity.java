package com.btxtech.server.persistence.tracker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Beat
 * on 01.01.2018.
 */
@Entity
@Table(name = "TRACKER_CONNECTION")
public class ConnectionTrackerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition = "DATETIME(3)")
    private Date systemOpen;
    @Column(columnDefinition = "DATETIME(3)")
    private Date systemClose;
    @Column(columnDefinition = "DATETIME(3)")
    private Date gameOpen;
    @Column(columnDefinition = "DATETIME(3)")
    private Date gameClose;
    @Column(length = 190) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String sessionId;
    private int humanPlayerId;

    public Date getSystemOpen() {
        return systemOpen;
    }

    public void setSystemOpen(Date systemOpen) {
        this.systemOpen = systemOpen;
    }

    public Date getSystemClose() {
        return systemClose;
    }

    public void setSystemClose(Date systemClose) {
        this.systemClose = systemClose;
    }

    public Date getGameOpen() {
        return gameOpen;
    }

    public void setGameOpen(Date gameOpen) {
        this.gameOpen = gameOpen;
    }

    public Date getGameClose() {
        return gameClose;
    }

    public void setGameClose(Date gameClose) {
        this.gameClose = gameClose;
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
