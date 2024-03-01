package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.quest.QuestConfigEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    public ServerLevelQuestEntryEntity orderColumn(Integer orderColumn) {
        setOrderColumn(orderColumn);
        return this;
    }

    public void setOrderColumn(Integer orderColumn) {
        this.orderColumn = orderColumn;
    }

}
