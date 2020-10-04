package com.btxtech.server.persistence.history;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Beat
 * 22.05.2017.
 */
// @Entity
// @Table(name = "HISTORY_USER")
public class UserHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition = "DATETIME(3)")
    private Date loggedIn;
    @Column(columnDefinition = "DATETIME(3)")
    private Date loggedOut;
    private int userId;
    @Column(length = 190)
    private String sessionId;

    public Date getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(Date loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Date getLoggedOut() {
        return loggedOut;
    }

    public void setLoggedOut(Date loggedOut) {
        this.loggedOut = loggedOut;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserHistoryEntity that = (UserHistoryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
