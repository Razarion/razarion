package com.btxtech.server.persistence.server;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.List;

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
    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "SERVER_QUEST",
            joinColumns = @JoinColumn(name = "serverLevelQuest"),
            inverseJoinColumns = @JoinColumn(name = "quest"))
    private List<QuestConfigEntity> questConfigs;
    @OneToOne(fetch = FetchType.LAZY)
    private LevelEntity minimalLevel;

    public Integer getId() {
        return id;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public List<QuestConfigEntity> getQuestConfigs() {
        return questConfigs;
    }

    public void setQuestConfigs(List<QuestConfigEntity> questConfigs) {
        this.questConfigs = questConfigs;
    }

    public LevelEntity getMinimalLevel() {
        return minimalLevel;
    }

    public void setMinimalLevel(LevelEntity minimalLevel) {
        this.minimalLevel = minimalLevel;
    }

    public ServerLevelQuestConfig toServerLevelQuestConfig() {
        ServerLevelQuestConfig serverLevelQuestConfig = new ServerLevelQuestConfig().setId(id).setInternalName(internalName);
        if (minimalLevel != null) {
            serverLevelQuestConfig.setMinimalLevelId(minimalLevel.getId());
        }
        return serverLevelQuestConfig;
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
