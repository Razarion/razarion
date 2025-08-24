package com.btxtech.shared.dto;

import java.util.Date;
import java.util.List;

public class UserBackendInfo {
    private String userId;
    private Date creationDate;
    private Date verificationStartedDate;
    private Date verificationDoneDate;
    private String email;
    private String name;
    private Integer activeQuest;
    private List<Integer> completedQuestIds;
    private Integer levelId;
    private Integer levelNumber;
    private int xp;
    private int crystals;
    private List<Integer> unlockedIds;
    private List<GameHistoryEntry> gameHistoryEntries;
    private Date systemConnectionOpened;
    private Date systemConnectionClosed;
    private boolean systemConnectionOpen;
    private Date systemConnectionLastMessageSent;
    private Date systemConnectionLastMessageReceived;
    private boolean gameConnectionOpen;
    private Date gameConnectionLastMessageSent;
    private Date gameConnectionLastMessageReceived;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getVerificationStartedDate() {
        return verificationStartedDate;
    }

    public void setVerificationStartedDate(Date verificationStartedDate) {
        this.verificationStartedDate = verificationStartedDate;
    }

    public Date getVerificationDoneDate() {
        return verificationDoneDate;
    }

    public void setVerificationDoneDate(Date verificationDoneDate) {
        this.verificationDoneDate = verificationDoneDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getActiveQuest() {
        return activeQuest;
    }

    public void setActiveQuest(Integer activeQuest) {
        this.activeQuest = activeQuest;
    }

    public List<Integer> getCompletedQuestIds() {
        return completedQuestIds;
    }

    public void setCompletedQuestIds(List<Integer> completedQuestIds) {
        this.completedQuestIds = completedQuestIds;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getCrystals() {
        return crystals;
    }

    public void setCrystals(int crystals) {
        this.crystals = crystals;
    }

    public List<Integer> getUnlockedIds() {
        return unlockedIds;
    }

    public void setUnlockedIds(List<Integer> unlockedIds) {
        this.unlockedIds = unlockedIds;
    }

    public List<GameHistoryEntry> getGameHistoryEntries() {
        return gameHistoryEntries;
    }

    public void setGameHistoryEntries(List<GameHistoryEntry> gameHistoryEntries) {
        this.gameHistoryEntries = gameHistoryEntries;
    }

    public Date getSystemConnectionOpened() {
        return systemConnectionOpened;
    }

    public void setSystemConnectionOpened(Date systemConnectionOpened) {
        this.systemConnectionOpened = systemConnectionOpened;
    }

    public Date getSystemConnectionClosed() {
        return systemConnectionClosed;
    }

    public void setSystemConnectionClosed(Date systemConnectionClosed) {
        this.systemConnectionClosed = systemConnectionClosed;
    }

    public boolean isSystemConnectionOpen() {
        return systemConnectionOpen;
    }

    public void setSystemConnectionOpen(boolean systemConnectionOpen) {
        this.systemConnectionOpen = systemConnectionOpen;
    }

    public Date getSystemConnectionLastMessageSent() {
        return systemConnectionLastMessageSent;
    }

    public void setSystemConnectionLastMessageSent(Date systemConnectionLastMessageSent) {
        this.systemConnectionLastMessageSent = systemConnectionLastMessageSent;
    }

    public Date getSystemConnectionLastMessageReceived() {
        return systemConnectionLastMessageReceived;
    }

    public void setSystemConnectionLastMessageReceived(Date systemConnectionLastMessageReceived) {
        this.systemConnectionLastMessageReceived = systemConnectionLastMessageReceived;
    }

    public boolean isGameConnectionOpen() {
        return gameConnectionOpen;
    }

    public void setGameConnectionOpen(boolean gameConnectionOpen) {
        this.gameConnectionOpen = gameConnectionOpen;
    }

    public Date getGameConnectionLastMessageSent() {
        return gameConnectionLastMessageSent;
    }

    public void setGameConnectionLastMessageSent(Date gameConnectionLastMessageSent) {
        this.gameConnectionLastMessageSent = gameConnectionLastMessageSent;
    }

    public Date getGameConnectionLastMessageReceived() {
        return gameConnectionLastMessageReceived;
    }

    public void setGameConnectionLastMessageReceived(Date gameConnectionLastMessageReceived) {
        this.gameConnectionLastMessageReceived = gameConnectionLastMessageReceived;
    }

    public UserBackendInfo userId(String userId) {
        setUserId(userId);
        return this;
    }

    public UserBackendInfo creationDate(Date creationDate) {
        setCreationDate(creationDate);
        return this;
    }

    public UserBackendInfo verificationStartedDate(Date registerDate) {
        setVerificationStartedDate(registerDate);
        return this;
    }

    public UserBackendInfo verificationDoneDate(Date verificationDoneDate) {
        setVerificationDoneDate(verificationDoneDate);
        return this;
    }

    public UserBackendInfo email(String email) {
        setEmail(email);
        return this;
    }

    public UserBackendInfo name(String name) {
        setName(name);
        return this;
    }

    public UserBackendInfo activeQuest(Integer activeQuest) {
        setActiveQuest(activeQuest);
        return this;
    }


    public UserBackendInfo completedQuestIds(List<Integer> completedQuests) {
        setCompletedQuestIds(completedQuests);
        return this;
    }

    public UserBackendInfo levelId(Integer levelId) {
        setLevelId(levelId);
        return this;
    }

    public UserBackendInfo levelNumber(Integer levelNumber) {
        setLevelNumber(levelNumber);
        return this;
    }

    public UserBackendInfo xp(int xp) {
        setXp(xp);
        return this;
    }

    public UserBackendInfo crystals(int crystals) {
        setCrystals(crystals);
        return this;
    }

    public UserBackendInfo unlockedIds(List<Integer> unlockedIds) {
        setUnlockedIds(unlockedIds);
        return this;
    }

    public UserBackendInfo gameHistoryEntries(List<GameHistoryEntry> gameHistoryEntries) {
        setGameHistoryEntries(gameHistoryEntries);
        return this;
    }

    public UserBackendInfo systemConnectionOpened(Date systemConnectionOpened) {
        setSystemConnectionOpened(systemConnectionOpened);
        return this;
    }

    public UserBackendInfo systemConnectionClosed(Date systemConnectionClosed) {
        setSystemConnectionClosed(systemConnectionClosed);
        return this;
    }

    public UserBackendInfo systemConnectionOpen(boolean systemConnectionOpen) {
        setSystemConnectionOpen(systemConnectionOpen);
        return this;
    }

    public UserBackendInfo systemConnectionLastMessageSent(Date systemConnectionLastMessageSent) {
        setSystemConnectionLastMessageSent(systemConnectionLastMessageSent);
        return this;
    }

    public UserBackendInfo systemConnectionLastMessageReceived(Date systemConnectionLastMessageReceived) {
        setSystemConnectionLastMessageReceived(systemConnectionLastMessageReceived);
        return this;
    }

    public UserBackendInfo gameConnectionOpen(boolean gameConnectionOpen) {
        setGameConnectionOpen(gameConnectionOpen);
        return this;
    }

    public UserBackendInfo gameConnectionLastMessageSent(Date gameConnectionLastMessageSent) {
        setGameConnectionLastMessageSent(gameConnectionLastMessageSent);
        return this;
    }

    public UserBackendInfo gameConnectionLastMessageReceived(Date gameConnectionLastMessageReceived) {
        setGameConnectionLastMessageReceived(gameConnectionLastMessageReceived);
        return this;
    }
}
