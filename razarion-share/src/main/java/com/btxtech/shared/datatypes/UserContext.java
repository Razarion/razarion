package com.btxtech.shared.datatypes;


import java.util.Map;

/**
 * Created by Beat
 * 30.08.2016.
 */
public class UserContext {
    public enum RegisterState {
        UNREGISTERED,
        EMAIL_UNVERIFIED,
        EMAIL_VERIFIED,
        FACEBOOK
    }

    private int userId;
    private RegisterState registerState;
    @Deprecated
    private HumanPlayerId humanPlayerId;
    private String name;
    private boolean admin;
    private Integer levelId;
    private Map<Integer, Integer> unlockedItemLimit;
    private int xp;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public UserContext userId(int userId) {
        setUserId(userId);
        return this;
    }

    public RegisterState getRegisterState() {
        return registerState;
    }

    public void setRegisterState(RegisterState registerState) {
        this.registerState = registerState;
    }

    public UserContext registerState(RegisterState registerState) {
        setRegisterState(registerState);
        return this;
    }

    @Deprecated
    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    @Deprecated
    public UserContext setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserContext setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public UserContext setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public UserContext setLevelId(Integer levelId) {
        this.levelId = levelId;
        return this;
    }

    public Map<Integer, Integer> getUnlockedItemLimit() {
        return unlockedItemLimit;
    }

    public UserContext setUnlockedItemLimit(Map<Integer, Integer> unlockedItemLimit) {
        this.unlockedItemLimit = unlockedItemLimit;
        return this;
    }

    public int getXp() {
        return xp;
    }

    public UserContext setXp(int xp) {
        this.xp = xp;
        return this;
    }

    public boolean registered() {
        return registerState != RegisterState.UNREGISTERED;
    }

    public boolean emailNotVerified() {
        return registerState != RegisterState.EMAIL_UNVERIFIED;
    }

    public boolean checkName() {
        return name != null;
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "humanPlayerId=" + humanPlayerId +
                ", name='" + name + '\'' +
                ", admin=" + admin +
                ", levelId=" + levelId +
                ", unlockedItemLimit=" + unlockedItemLimit +
                ", xp=" + xp +
                '}';
    }
}
