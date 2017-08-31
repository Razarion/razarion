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
 * 22.05.2017.
 */
@Entity
@Table(name = "HISTORY_QUEST")
public class QuestHistoryEntity {
    public enum Type {
        QUEST_ACTIVATED,
        QUEST_DEACTIVATED,
        QUEST_PASSED
        }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private Date timeStamp;
    @Enumerated(EnumType.STRING)
    private Type type;
    private int humanPlayerIdEntityId;
    private int questId;
    private String questInternalName;

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

    public int getHumanPlayerIdEntityId() {
        return humanPlayerIdEntityId;
    }

    public void setHumanPlayerIdEntityId(int humanPlayerIdEntityId) {
        this.humanPlayerIdEntityId = humanPlayerIdEntityId;
    }

    public int getQuestId() {
        return questId;
    }

    public void setQuestId(int questId) {
        this.questId = questId;
    }

    public String getQuestInternalName() {
        return questInternalName;
    }

    public void setQuestInternalName(String questInternalName) {
        this.questInternalName = questInternalName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QuestHistoryEntity that = (QuestHistoryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
