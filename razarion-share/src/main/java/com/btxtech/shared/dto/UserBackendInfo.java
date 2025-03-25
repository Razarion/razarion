package com.btxtech.shared.dto;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 06.09.2017.
 */
public class UserBackendInfo {
    private int userId;
    private Date creationDate;
    private Date registerDate;
    private Date verificationDoneDate;
    private String facebookId;
    private String email;
    private String name;
    private Integer activeQuest;
    private List<Integer> completedQuestIds;
    private Integer levelId;
    private int xp;
    private int crystals;
    private List<Integer> unlockedIds;
    private List<GameHistoryEntry> gameHistoryEntries;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public Date getVerificationDoneDate() {
        return verificationDoneDate;
    }

    public void setVerificationDoneDate(Date verificationDoneDate) {
        this.verificationDoneDate = verificationDoneDate;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
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

    public UserBackendInfo userId(int userId) {
        setUserId(userId);
        return this;
    }

    public UserBackendInfo creationDate(Date creationDate) {
        setCreationDate(creationDate);
        return this;
    }

    public UserBackendInfo registerDate(Date registerDate) {
        setRegisterDate(registerDate);
        return this;
    }

    public UserBackendInfo verificationDoneDate(Date verificationDoneDate) {
        setVerificationDoneDate(verificationDoneDate);
        return this;
    }

    public UserBackendInfo facebookId(String facebookId) {
        setFacebookId(facebookId);
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

    public UserBackendInfo levelId(Integer levelNumber) {
        setLevelId(levelNumber);
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
}
