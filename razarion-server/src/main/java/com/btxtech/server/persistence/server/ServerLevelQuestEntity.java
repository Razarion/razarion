package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.itemtype.BaseItemTypeCrudPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.dto.ServerLevelQuestConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.List;
import java.util.Locale;

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
    @OrderColumn(name = "orderColumn")
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "SERVER_QUEST",
            joinColumns = @JoinColumn(name = "serverLevelQuest"),
            inverseJoinColumns = @JoinColumn(name = "quest"))
    private List<QuestConfigEntity> questConfigs;
    @OneToOne(fetch = FetchType.LAZY)
    private LevelEntity minimalLevel;

    public Integer getId() {
        return id;
    }

    public LevelEntity getMinimalLevel() {
        return minimalLevel;
    }

    public ServerLevelQuestConfig toServerLevelQuestConfig(Locale locale) {
        return new ServerLevelQuestConfig()
                .id(id)
                .internalName(internalName)
                .minimalLevelId(extractId(minimalLevel, LevelEntity::getId))
                .questConfigs(toConfigList(questConfigs, questConfigEntity -> questConfigEntity.toQuestConfig(locale)));
    }

    public void fromServerLevelQuestConfig(ItemTypePersistence itemTypePersistence, BaseItemTypeCrudPersistence baseItemTypeCrudPersistence, ServerLevelQuestConfig serverLevelQuestConfig, LevelCrudPersistence levelCrudPersistence, Locale locale) {
        internalName = serverLevelQuestConfig.getInternalName();
        minimalLevel = levelCrudPersistence.getEntity(serverLevelQuestConfig.getMinimalLevelId());
        questConfigs = fromConfigs(questConfigs,
                serverLevelQuestConfig.getQuestConfigs(),
                QuestConfigEntity::new,
                (questConfigEntity, questConfig) -> questConfigEntity.fromQuestConfig(itemTypePersistence, baseItemTypeCrudPersistence, questConfig, locale));
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
