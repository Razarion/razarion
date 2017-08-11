package com.btxtech.server.user;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Entity
@Table(name = "USER", indexes = {@Index(columnList = "facebookUserId")})
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 190)
    // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String facebookUserId;
    private Date registerDate;
    private boolean admin;
    @OneToOne(fetch = FetchType.LAZY)
    private HumanPlayerIdEntity humanPlayerIdEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    private LevelEntity level;
    @ManyToOne(fetch = FetchType.LAZY)
    private QuestConfigEntity activeQuest;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_COMPLETED_QUEST",
            joinColumns = @JoinColumn(name = "user"),
            inverseJoinColumns = @JoinColumn(name = "quest"))
    private List<QuestConfigEntity> completedQuest;
    private Locale locale;

    public Integer getId() {
        return id;
    }

    public void fromFacebookUserLoginInfo(String facebookUserId, HumanPlayerIdEntity humanPlayerId, Locale locale) {
        registerDate = new Date();
        this.facebookUserId = facebookUserId;
        this.humanPlayerIdEntity = humanPlayerId;
        this.locale = locale;
    }

    public UserContext createUser() {
        return new UserContext().setName("Registered User").setHumanPlayerId(createHumanPlayerId()).setLevelId(level.getId()).setAdmin(admin);
    }

    public HumanPlayerId createHumanPlayerId() {
        return new HumanPlayerId().setPlayerId(humanPlayerIdEntity.getId()).setUserId(id);
    }

    public LevelEntity getLevel() {
        return level;
    }

    public void setLevel(LevelEntity level) {
        this.level = level;
    }

    public QuestConfigEntity getActiveQuest() {
        return activeQuest;
    }

    public void setActiveQuest(QuestConfigEntity activeQuest) {
        this.activeQuest = activeQuest;
    }

    public void addCompletedQuest(QuestConfigEntity quest) {
        if (completedQuest == null) {
            completedQuest = new ArrayList<>();
        }
        completedQuest.add(quest);
    }

    public List<QuestConfigEntity> getCompletedQuest() {
        return completedQuest;
    }

    public List<Integer> getCompletedQuestIds() {
        if (completedQuest != null) {
            return completedQuest.stream().map(QuestConfigEntity::getId).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserEntity that = (UserEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
