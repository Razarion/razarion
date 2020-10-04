package com.btxtech.server.persistence.history;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Beat
 * on 01.02.2018.
 */
// @Entity
// @Table(name = "HISTORY_FORGOT_PASSWORDY")
public class ForgotPasswordHistoryEntity {
    public enum Type{
        INITIATED,
        TIMED_OUT,
        OVERRIDDEN,
        CHANGED
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int userId;
    private int humanPlayerId;
    @Column(nullable = false, columnDefinition = "DATETIME(3)")
    private Date timeStamp;
    private Type type;
    private int forgotPasswordEntityId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getHumanPlayerId() {
        return humanPlayerId;
    }

    public void setHumanPlayerId(int humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
    }

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

    public int getForgotPasswordEntityId() {
        return forgotPasswordEntityId;
    }

    public void setForgotPasswordEntityId(int forgotPasswordEntityId) {
        this.forgotPasswordEntityId = forgotPasswordEntityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ForgotPasswordHistoryEntity that = (ForgotPasswordHistoryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
