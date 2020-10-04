package com.btxtech.server.mgmt;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 06.09.2017.
 */
public class UserBackendInfo {
    private int userId;
    private Date registerDate;
    private String facebookId;
    private String email;
    private String name;
    private QuestBackendInfo activeQuest;
    private List<QuestBackendInfo> completedQuests;
    private int levelNumber;
    private int xp;
    private int crystals;
    private List<UnlockedBackendInfo> unlockedBackendInfos;
    private List<GameHistoryEntry> gameHistoryEntries;


    public int getUserId() {
        return userId;
    }

    public UserBackendInfo setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public UserBackendInfo setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
        return this;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public UserBackendInfo setFacebookId(String facebookId) {
        this.facebookId = facebookId;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserBackendInfo setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserBackendInfo setName(String name) {
        this.name = name;
        return this;
    }

    public QuestBackendInfo getActiveQuest() {
        return activeQuest;
    }

    public UserBackendInfo setActiveQuest(QuestBackendInfo activeQuest) {
        this.activeQuest = activeQuest;
        return this;
    }

    public List<QuestBackendInfo> getCompletedQuests() {
        return completedQuests;
    }

    public UserBackendInfo setCompletedQuests(List<QuestBackendInfo> completedQuests) {
        this.completedQuests = completedQuests;
        return this;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public UserBackendInfo setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
        return this;
    }

    public int getXp() {
        return xp;
    }

    public UserBackendInfo setXp(int xp) {
        this.xp = xp;
        return this;
    }

    public int getCrystals() {
        return crystals;
    }

    public UserBackendInfo setCrystals(int crystals) {
        this.crystals = crystals;
        return this;
    }

    public List<UnlockedBackendInfo> getUnlockedBackendInfos() {
        return unlockedBackendInfos;
    }

    public UserBackendInfo setUnlockedBackendInfos(List<UnlockedBackendInfo> unlockedBackendInfos) {
        this.unlockedBackendInfos = unlockedBackendInfos;
        return this;
    }

    public List<GameHistoryEntry> getGameHistoryEntries() {
        return gameHistoryEntries;
    }

    public UserBackendInfo setGameHistoryEntries(List<GameHistoryEntry> gameHistoryEntries) {
        this.gameHistoryEntries = gameHistoryEntries;
        return this;
    }
}
