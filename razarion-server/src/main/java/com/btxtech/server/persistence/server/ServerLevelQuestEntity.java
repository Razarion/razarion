package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.BotConfigEntityPersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.btxtech.server.persistence.PersistenceUtil.*;

/**
 * Created by Beat
 * on 01.08.2017.
 */
@Entity
@Table(name = "SERVER_LEVEL_QUEST")
public class ServerLevelQuestEntity implements ObjectNameIdProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OrderBy("orderColumn")
    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "serverLevelQuest", nullable = false)
    private List<ServerLevelQuestEntryEntity> serverLevelQuestEntryEntities;
    @OneToOne(fetch = FetchType.LAZY)
    private LevelEntity minimalLevel;

    public Integer getId() {
        return id;
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
                .id(id)
                .internalName(internalName)
                .minimalLevelId(extractId(minimalLevel, LevelEntity::getId))
                .questConfigs(toConfigList(serverLevelQuestEntryEntities, questConfigEntity -> questConfigEntity.getQuest().toQuestConfig()));
    }

    public void fromServerLevelQuestConfig(BotConfigEntityPersistence botConfigEntityPersistence,
                                           BaseItemTypeCrudPersistence baseItemTypeCrudPersistence,
                                           ServerLevelQuestConfig serverLevelQuestConfig,
                                           LevelCrudPersistence levelCrudPersistence) {
        internalName = serverLevelQuestConfig.getInternalName();
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
        return new ObjectNameId(id, internalName);
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
