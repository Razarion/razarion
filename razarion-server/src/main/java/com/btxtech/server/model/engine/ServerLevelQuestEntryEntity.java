package com.btxtech.server.model.engine;


import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "SERVER_LEVEL_QUEST_ENTRY")
public class ServerLevelQuestEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @JoinColumn
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private QuestConfigEntity quest;
    private Integer orderColumn;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public QuestConfigEntity getQuest() {
        return quest;
    }

    public void setQuest(QuestConfigEntity quest) {
        this.quest = quest;
    }

    public ServerLevelQuestEntryEntity quest(QuestConfigEntity quest) {
        setQuest(quest);
        return this;
    }

    public Integer getOrderColumn() {
        return orderColumn;
    }

    public void setOrderColumn(Integer orderColumn) {
        this.orderColumn = orderColumn;
    }

    public ServerLevelQuestEntryEntity orderColumn(Integer orderColumn) {
        setOrderColumn(orderColumn);
        return this;
    }

}
