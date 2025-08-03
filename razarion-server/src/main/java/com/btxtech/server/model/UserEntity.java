package com.btxtech.server.model;

import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.model.engine.InventoryItemEntity;
import com.btxtech.server.model.engine.LevelEntity;
import com.btxtech.server.model.engine.LevelUnlockEntity;
import com.btxtech.server.model.engine.quest.QuestConfigEntity;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.InventoryInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "RAZARION_USER")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 190)
    private String email;
    @Column(length = 190)
    private String passwordHash;
    @Column(columnDefinition = "DATETIME(3)")
    private Date verificationStartedDate;
    @Column(columnDefinition = "DATETIME(3)")
    private Date verificationDoneDate;
    @Column(columnDefinition = "DATETIME(3)")
    private Date verificationTimedOutDate;
    @Column(length = 190)
// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String verificationId;
    @Column(length = 190)
// Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String facebookUserId;
    @Column(columnDefinition = "DATETIME(3)")
    private Date registerDate;
    @Column(columnDefinition = "DATETIME(3)")
    private Date creationDate;
    private String userId;
    private boolean admin;
    @ManyToOne(fetch = FetchType.LAZY)
    private LevelEntity level;
    @ManyToOne(fetch = FetchType.LAZY)
    private QuestConfigEntity activeQuest;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_COMPLETED_QUEST",
            joinColumns = @JoinColumn(name = "razarion-user"),
            inverseJoinColumns = @JoinColumn(name = "quest"))
    private List<QuestConfigEntity> completedQuest;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_INVENTORY",
            joinColumns = @JoinColumn(name = "razarion-user"),
            inverseJoinColumns = @JoinColumn(name = "inventory"))
    private List<InventoryItemEntity> inventory;
    private int xp;
    private int crystals;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_UNLOCKED",
            joinColumns = @JoinColumn(name = "razarion-user"),
            inverseJoinColumns = @JoinColumn(name = "levelUnlockEntity"))
    private List<LevelUnlockEntity> levelUnlockEntities;
    @Column(unique = true)
    private String name;
    private LocalDateTime systemConnectionOpened;
    private LocalDateTime systemConnectionClosed;

    public Integer getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return admin;
    }

    public UserContext toUserContext() {
        UserContext userContext = new UserContext()
                .userId(userId)
                .registerState(createRegisterState())
                .name(name)
                .unlockedItemLimit(ServerUnlockService.convertUnlockedItemLimit(levelUnlockEntities))
                .xp(xp);
        if (level != null) {
            userContext.levelId(level.getId());
        }
        return userContext;
    }

    public LevelEntity getLevel() {
        return level;
    }

    public void setLevel(LevelEntity level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
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

    public void setCompletedQuest(List<QuestConfigEntity> completedQuest) {
        if (this.completedQuest != null) {
            this.completedQuest.clear();
            this.completedQuest.addAll(completedQuest);
        } else {
            this.completedQuest = completedQuest;
        }
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

    public String getFacebookUserId() {
        return facebookUserId;
    }

    public Date getVerificationDoneDate() {
        return verificationDoneDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDateDate) {
        this.creationDate = creationDateDate;
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

    public List<LevelUnlockEntity> getLevelUnlockEntities() {
        return levelUnlockEntities;
    }

    public void setLevelUnlockEntities(List<LevelUnlockEntity> levelUnlockEntities) {
        if (this.levelUnlockEntities == null) {
            this.levelUnlockEntities = new ArrayList<>();
        }
        this.levelUnlockEntities.clear();
        this.levelUnlockEntities.addAll(levelUnlockEntities);
    }

    public void startVerification() {
        verificationStartedDate = new Date();
        verificationId = UUID.randomUUID().toString().toUpperCase();
    }

    public void setVerifiedDone() {
        verificationDoneDate = new Date();
    }

    public void setVerifiedTimedOut() {
        verificationTimedOutDate = new Date();
    }

    public boolean isVerified() {
        UserContext.RegisterState registerState = createRegisterState();
        return registerState == UserContext.RegisterState.FACEBOOK
                || registerState == UserContext.RegisterState.EMAIL_VERIFIED;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public Date getVerificationTimedOutDate() {
        return verificationTimedOutDate;
    }

    public UserContext.RegisterState createRegisterState() {
        if (facebookUserId != null) {
            return UserContext.RegisterState.FACEBOOK;
        } else if (verificationDoneDate != null) {
            return UserContext.RegisterState.EMAIL_VERIFIED;
        } else if (verificationStartedDate != null && verificationTimedOutDate == null) {
            return UserContext.RegisterState.EMAIL_UNVERIFIED;
        } else {
            return UserContext.RegisterState.UNREGISTERED;
        }
    }

    public LocalDateTime getSystemConnectionOpened() {
        return systemConnectionOpened;
    }

    public void setSystemConnectionOpened(LocalDateTime systemConnectionOpened) {
        this.systemConnectionOpened = systemConnectionOpened;
    }

    public LocalDateTime getSystemConnectionClosed() {
        return systemConnectionClosed;
    }

    public void setSystemConnectionClosed(LocalDateTime systemConnectionClosed) {
        this.systemConnectionClosed = systemConnectionClosed;
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

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", userId=" + userId +
                ", creationDate=" + creationDate +
                ", systemConnectionOpened=" + systemConnectionOpened +
                ", systemConnectionClosed=" + systemConnectionClosed +
                '}';
    }
}
