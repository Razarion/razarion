package com.btxtech.server.persistence.history;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Beat
 * on 04.04.2018.
 */
@Entity
@Table(name = "HISTORY_BOT_SCENE_INDICATOR")
public class BotSceneIndicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition = "DATETIME(3)")
    private Date timeStamp;
    private int humanPlayerIdEntityId;
    private boolean raise;
    private Integer newBotSceneConflictConfigId;
    private Integer oldBotSceneConflictConfigId;
    private Integer botSceneId;
    private Integer step;
    private Integer stepCount;


    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setHumanPlayerIdEntityId(int humanPlayerIdEntityId) {
        this.humanPlayerIdEntityId = humanPlayerIdEntityId;
    }

    public boolean isRaise() {
        return raise;
    }

    public void setRaise(boolean raise) {
        this.raise = raise;
    }

    public Integer getNewBotSceneConflictConfigId() {
        return newBotSceneConflictConfigId;
    }

    public void setNewBotSceneConflictConfigId(Integer newBotSceneConflictConfigId) {
        this.newBotSceneConflictConfigId = newBotSceneConflictConfigId;
    }

    public Integer getOldBotSceneConflictConfigId() {
        return oldBotSceneConflictConfigId;
    }

    public void setOldBotSceneConflictConfigId(Integer oldBotSceneConflictConfigId) {
        this.oldBotSceneConflictConfigId = oldBotSceneConflictConfigId;
    }

    public Integer getBotSceneId() {
        return botSceneId;
    }

    public void setBotSceneId(Integer botSceneId) {
        this.botSceneId = botSceneId;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getStepCount() {
        return stepCount;
    }

    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotSceneIndicationEntity that = (BotSceneIndicationEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
