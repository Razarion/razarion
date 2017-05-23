package com.btxtech.server.user;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FacebookUserLoginInfo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Entity
@Table(name = "USER", indexes = {@Index(columnList = "facebookUserId")})
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 190)// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String facebookUserId;
    private Date registerDate;
    private boolean admin;
    @OneToOne
    private HumanPlayerIdEntity humanPlayerIdEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    LevelEntity level;

    public Integer getId() {
        return id;
    }

    public void fromFacebookUserLoginInfo(String facebookUserId, HumanPlayerIdEntity humanPlayerId) {
        registerDate = new Date();
        this.facebookUserId = facebookUserId;
        this.humanPlayerIdEntity = humanPlayerId;
    }

    public UserContext createUser() {
        HumanPlayerId humanPlayerId = new HumanPlayerId().setPlayerId(humanPlayerIdEntity.getId()).setUserId(id);
        return new UserContext().setName("Registered User").setHumanPlayerId(humanPlayerId).setLevelId(level.getId()).setAdmin(admin);
    }

    public LevelEntity getLevel() {
        return level;
    }

    public void setLevel(LevelEntity level) {
        this.level = level;
    }

    public HumanPlayerIdEntity getHumanPlayerIdEntity() {
        return humanPlayerIdEntity;
    }

    public void setHumanPlayerIdEntity(HumanPlayerIdEntity humanPlayerIdEntity) {
        this.humanPlayerIdEntity = humanPlayerIdEntity;
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
