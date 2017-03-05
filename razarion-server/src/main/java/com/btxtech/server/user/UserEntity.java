package com.btxtech.server.user;

import com.btxtech.shared.dto.FacebookUserLoginInfo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Entity
@Table(name = "USER", indexes = { @Index(columnList = "facebookUserId") })
public class UserEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 190)// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String facebookUserId;
    private Date registerDate;
    private boolean admin;

    public Long getId() {
        return id;
    }

    public void fromFacebookUserLoginInfo(FacebookUserLoginInfo facebookUserLoginInfo) {
        facebookUserId = facebookUserLoginInfo.getUserId();
        registerDate = new Date();
    }

    public User createUser() {
        return new User(id, admin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserEntity that = (UserEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
