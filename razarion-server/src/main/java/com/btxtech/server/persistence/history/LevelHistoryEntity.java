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
// @Table(name = "HISTORY_LEVEL")
public class LevelHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, columnDefinition = "DATETIME(3)")
    private Date timeStamp;
    private int humanPlayerIdEntityId;
    private int levelId;
    private int levelNumber;

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getHumanPlayerIdEntityId() {
        return humanPlayerIdEntityId;
    }

    public void setHumanPlayerIdEntityId(int humanPlayerIdEntityId) {
        this.humanPlayerIdEntityId = humanPlayerIdEntityId;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LevelHistoryEntity that = (LevelHistoryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
