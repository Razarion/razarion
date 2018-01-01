package com.btxtech.server.user;

import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.InventoryInfo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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
    @Column(columnDefinition = "DATETIME(3)")
    private Date registerDate;
    private boolean admin;
    @OneToOne(fetch = FetchType.LAZY)
    private HumanPlayerIdEntity humanPlayerIdEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    private LevelEntity level;
    @ManyToOne(fetch = FetchType.LAZY)
    private QuestConfigEntity activeQuest;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_COMPLETED_QUEST",
            joinColumns = @JoinColumn(name = "user"),
            inverseJoinColumns = @JoinColumn(name = "quest"))
    private List<QuestConfigEntity> completedQuest;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_INVENTORY",
            joinColumns = @JoinColumn(name = "user"),
            inverseJoinColumns = @JoinColumn(name = "inventory"))
    private List<InventoryItemEntity> inventory;
    private Locale locale;
    private int xp;
    private int crystals;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_UNLOCKED",
            joinColumns = @JoinColumn(name = "user"),
            inverseJoinColumns = @JoinColumn(name = "levelUnlockEntity"))
    private List<LevelUnlockEntity> levelUnlockEntities;
    @Column(unique=true)
    private String name;

    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void fromFacebookUserLoginInfo(String facebookUserId, HumanPlayerIdEntity humanPlayerId, Locale locale) {
        registerDate = new Date();
        this.facebookUserId = facebookUserId;
        this.humanPlayerIdEntity = humanPlayerId;
        this.locale = locale;
    }

    public UserContext toUserContext() {
        return new UserContext().setName(name).setHumanPlayerId(createHumanPlayerId()).setLevelId(level.getId()).setUnlockedItemLimit(ServerUnlockService.convertUnlockedItemLimit(levelUnlockEntities)).setAdmin(admin).setXp(xp);
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

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getXp() {
        return xp;
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

    public void removeCompletedQuest(QuestConfigEntity quest) {
        if (completedQuest == null) {
            return;
        }
        completedQuest.remove(quest);
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

    public void addInventoryItem(InventoryItemEntity inventoryItemEntity) {
        if (inventory == null) {
            inventory = new ArrayList<>();
        }
        inventory.add(inventoryItemEntity);
    }

    public void removeInventoryItem(InventoryItemEntity inventoryItemEntity) {
        if (inventory == null) {
            return;
        }
        inventory.remove(inventoryItemEntity);
    }

    public Locale getLocale() {
        return locale;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public int getCrystals() {
        return crystals;
    }

    public void setCrystals(int crystals) {
        this.crystals = crystals;
    }

    public void addCrystals(int crystals) {
        this.crystals += crystals;
    }

    public void removeCrystals(int crystals) {
        this.crystals -= crystals;
    }

    public InventoryInfo toInventoryInfo() {
        InventoryInfo inventoryInfo = new InventoryInfo();
        inventoryInfo.setCrystals(crystals);
        if (inventory != null) {
            inventoryInfo.setInventoryItemIds(inventory.stream().map(InventoryItemEntity::getId).collect(Collectors.toList()));
        }
        return inventoryInfo;
    }

    public void addLevelUnlockEntity(LevelUnlockEntity levelUnlockEntity) {
        if (levelUnlockEntities == null) {
            levelUnlockEntities = new ArrayList<>();
        }
        if (levelUnlockEntities.contains(levelUnlockEntity)) {
            throw new IllegalArgumentException("User already has unlocked LevelUnlockEntity with id: " + levelUnlockEntity.getId() + " UserEntity id: " + id);
        }
        levelUnlockEntities.add(levelUnlockEntity);
    }

    public void setLevelUnlockEntities(List<LevelUnlockEntity> levelUnlockEntities) {
        if(this.levelUnlockEntities == null) {
            this.levelUnlockEntities = new ArrayList<>();
        }
        this.levelUnlockEntities.clear();
        this.levelUnlockEntities.addAll(levelUnlockEntities);
    }

    public List<LevelUnlockEntity> getLevelUnlockEntities() {
        return levelUnlockEntities;
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
