package com.btxtech.server.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Beat
 * on 31.01.2018.
 */
@Entity
@Table(name = "USER_FORGOT_PASSWORD")
public class ForgotPasswordEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne(optional = false)
    private UserEntity user;
    @Column(nullable = false, length = 190)// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String uuid;
    @Column(columnDefinition = "DATETIME(3)")
    private Date date;

    public Integer getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ForgotPasswordEntity that = (ForgotPasswordEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
