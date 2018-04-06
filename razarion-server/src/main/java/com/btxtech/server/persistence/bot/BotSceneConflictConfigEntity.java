package com.btxtech.server.persistence.bot;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConflictConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * on 23.03.2018.
 */
@Entity
@Table(name = "BOT_SCENE_CONFLICT_CONFIG")
public class BotSceneConflictConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int enterKills; // ms
    private int enterDuration;
    private int leaveNoKillDuration; // ms
    private Integer rePopMillis;
    private double minDistance;
    private double maxDistance;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity targetBaseItemType;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BotConfigEntity botConfig;
    private Integer stopKills;
    private Integer stopMillis;

    public Integer getId() {
        return id;
    }

    public BotSceneConflictConfig toBotSceneConflictConfig() {
        BotSceneConflictConfig botSceneConflictConfig = new BotSceneConflictConfig().setId(id).setEnterKills(enterKills).setEnterDuration(enterDuration).setLeaveNoKillDuration(leaveNoKillDuration).setRePopMillis(rePopMillis).setMinDistance(minDistance).setMaxDistance(maxDistance);
        if (targetBaseItemType != null) {
            botSceneConflictConfig.setTargetBaseItemTypeId(targetBaseItemType.getId());
        }
        if (botConfig != null) {
            botSceneConflictConfig.setBotConfig(botConfig.toBotConfig());
        }
        return botSceneConflictConfig.setStopKills(stopKills).setStopMillis(stopMillis);
    }

    public BotSceneConflictConfigEntity fromBotSceneConflictConfig(ItemTypePersistence itemTypePersistence, BotSceneConflictConfig botSceneConflictConfig) {
        minDistance = botSceneConflictConfig.getMinDistance();
        maxDistance = botSceneConflictConfig.getMaxDistance();
        enterKills = botSceneConflictConfig.getEnterKills();
        enterDuration = botSceneConflictConfig.getEnterDuration();
        leaveNoKillDuration = botSceneConflictConfig.getLeaveNoKillDuration();
        rePopMillis = botSceneConflictConfig.getRePopMillis();
        targetBaseItemType = itemTypePersistence.readBaseItemTypeEntity(botSceneConflictConfig.getTargetBaseItemTypeId());
        if (botSceneConflictConfig.getBotConfig() != null) {
            if (botConfig == null) {
                botConfig = new BotConfigEntity();
            }
            botConfig.fromBotConfig(itemTypePersistence, botSceneConflictConfig.getBotConfig());
        } else {
            botConfig = null;
        }
        stopKills = botSceneConflictConfig.getStopKills();
        stopMillis = botSceneConflictConfig.getStopMillis();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotSceneConflictConfigEntity that = (BotSceneConflictConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
