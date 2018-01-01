package com.btxtech.server.persistence.history;

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
 * on 18.09.2017.
 */
@Entity
@Table(name = "HISTORY_UNLOCKED")
public class LevelUnlockHistoryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, columnDefinition = "DATETIME(3)")
    private Date timeStamp;
    private int humanPlayerIdEntityId;
    private Integer crystals;
    private Integer unlockEntityId;
    private String unlockEntityName;

    public void setHumanPlayerIdEntityId(int humanPlayerIdEntityId) {
        this.humanPlayerIdEntityId = humanPlayerIdEntityId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getCrystals() {
        return crystals;
    }

    public void setCrystals(Integer crystals) {
        this.crystals = crystals;
    }

    public void setUnlockEntityId(Integer unlockEntityId) {
        this.unlockEntityId = unlockEntityId;
    }

    public void setUnlockEntityName(String unlockEntityName) {
        this.unlockEntityName = unlockEntityName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LevelUnlockHistoryEntry that = (LevelUnlockHistoryEntry) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
