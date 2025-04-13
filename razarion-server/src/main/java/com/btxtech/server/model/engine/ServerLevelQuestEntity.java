package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.server.service.engine.BaseItemTypeCrudPersistence;
import com.btxtech.server.service.engine.BotConfigEntityPersistence;
import com.btxtech.server.service.engine.LevelCrudPersistence;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import jakarta.persistence.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.btxtech.server.service.PersistenceUtil.*;


@Entity
@Table(name = "SERVER_LEVEL_QUEST")
public class ServerLevelQuestEntity extends BaseEntity implements ObjectNameIdProvider {
    @OrderBy("orderColumn")
    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverLevelQuest", nullable = false)
    private List<ServerLevelQuestEntryEntity> serverLevelQuestEntryEntities;
    @OneToOne(fetch = FetchType.LAZY)
    private LevelEntity minimalLevel;

    public LevelEntity getMinimalLevel() {
        return minimalLevel;
    }

    public void setMinimalLevel(LevelEntity minimalLevel) {
        this.minimalLevel = minimalLevel;
    }

    public List<ServerLevelQuestEntryEntity> getServerLevelQuestEntryEntities() {
        return serverLevelQuestEntryEntities;
    }

    public void setServerLevelQuestEntryEntities(List<ServerLevelQuestEntryEntity> serverLevelQuestEntryEntities) {
        this.serverLevelQuestEntryEntities = serverLevelQuestEntryEntities;
    }

    public ServerLevelQuestConfig toServerLevelQuestConfig() {
        return new ServerLevelQuestConfig()
                .id(getId())
                .internalName(getInternalName())
                .minimalLevelId(extractId(minimalLevel, LevelEntity::getId))
                .questConfigs(toConfigList(serverLevelQuestEntryEntities, questConfigEntity -> questConfigEntity.getQuest().toQuestConfig()));
    }

    public void fromServerLevelQuestConfig(BotConfigEntityPersistence botConfigEntityPersistence,
                                           BaseItemTypeCrudPersistence baseItemTypeCrudPersistence,
                                           ServerLevelQuestConfig serverLevelQuestConfig,
                                           LevelCrudPersistence levelCrudPersistence) {
        setInternalName(serverLevelQuestConfig.getInternalName());
        minimalLevel = levelCrudPersistence.getEntity(serverLevelQuestConfig.getMinimalLevelId());

        Map<QuestConfig, Integer> savedOrder = new HashMap<>();
        List<QuestConfig> questConfigs = serverLevelQuestConfig.getQuestConfigs();
        for (int i = 0; i < questConfigs.size(); i++) {
            QuestConfig questConfig = questConfigs.get(i);
            savedOrder.put(questConfig, i);
        }

        serverLevelQuestEntryEntities = fromConfigsNoClear(serverLevelQuestEntryEntities,
                serverLevelQuestConfig.getQuestConfigs(),
                () -> new ServerLevelQuestEntryEntity().quest(new QuestConfigEntity()),
                (serverLevelQuestEntryEntity, questConfig) -> {
                    serverLevelQuestEntryEntity.getQuest().fromQuestConfig(botConfigEntityPersistence, baseItemTypeCrudPersistence, questConfig);
                    serverLevelQuestEntryEntity.setOrderColumn(savedOrder.get(questConfig));
                },
                QuestDescriptionConfig::getId,
                serverLevelQuestEntryEntity -> serverLevelQuestEntryEntity.getQuest().getId());
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(getId(), getInternalName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerLevelQuestEntity that = (ServerLevelQuestEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

}
