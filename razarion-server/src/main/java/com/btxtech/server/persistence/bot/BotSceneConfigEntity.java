package com.btxtech.server.persistence.bot;

import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 23.03.2018.
 */
@Entity
@Table(name = "BOT_SCENE_CONFIG")
public class BotSceneConfigEntity implements ObjectNameIdProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private int killThreshold;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "BOT_SCENE_CONFIG_BOTS_TO_WATCH",
            joinColumns = @JoinColumn(name = "botScene"),
            inverseJoinColumns = @JoinColumn(name = "bot"))
    private List<BotConfigEntity> botsToWatch;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "botConfig", nullable = false)
    private List<BotSceneConflictConfigEntity> botSceneConflictConfigs;

    public Integer getId() {
        return id;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

    public BotSceneConfig toBotSceneConfig() {
        BotSceneConfig botSceneConfig = new BotSceneConfig().setId(id).setInternalName(internalName).setKillThreshold(killThreshold).setScheduleTimeMillis(3000);
        if (botsToWatch != null) {
            botSceneConfig.setBotIdsToWatch(botsToWatch.stream().map(BotConfigEntity::getId).collect(Collectors.toList()));
        }
        if (botSceneConflictConfigs != null) {
            botSceneConfig.setBotSceneConflictConfigs(botSceneConflictConfigs.stream().map(BotSceneConflictConfigEntity::toBotSceneConflictConfig).collect(Collectors.toList()));
        }
        return botSceneConfig;
    }

    public void fromBotConfig(ItemTypePersistence itemTypePersistence, EntityManager entityManager, BotSceneConfig botSceneConfig) {
        internalName = botSceneConfig.getInternalName();
        killThreshold = botSceneConfig.getKillThreshold();
        botsToWatch.clear();
        if (botSceneConfig.getBotIdsToWatch() != null) {
            botSceneConfig.getBotIdsToWatch().forEach(botId -> botsToWatch.add(entityManager.find(BotConfigEntity.class, botId)));
        }
        botSceneConflictConfigs.clear();
        if (botSceneConfig.getBotSceneConflictConfigs() != null) {
            botSceneConfig.getBotSceneConflictConfigs().forEach(botSceneConflictConfig -> {
                botSceneConflictConfigs.add(new BotSceneConflictConfigEntity().fromBotSceneConflictConfig(itemTypePersistence, botSceneConflictConfig));
            });
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotSceneConfigEntity that = (BotSceneConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
