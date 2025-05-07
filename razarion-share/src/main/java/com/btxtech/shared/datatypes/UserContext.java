package com.btxtech.shared.datatypes;


import org.dominokit.jackson.annotation.JSONMapper;

import java.util.Map;

/**
 * Created by Beat
 * 30.08.2016.
 */
@JSONMapper
public class UserContext {
    private int userId;
    private RegisterState registerState;
    private String name;
    private Integer levelId;
    private Map<Integer, Integer> unlockedItemLimit;
    private int xp;

    public int getUserId() {
        return userId;
    }

    public UserContext setUserId(int userId) {
        this.userId = userId;
        return this;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserContext name(String name) {
        setName(name);
        return this;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public UserContext levelId(Integer levelId) {
        setLevelId(levelId);
        return this;
    }

    public Map<Integer, Integer> getUnlockedItemLimit() {
        return unlockedItemLimit;
    }

    public void setUnlockedItemLimit(Map<Integer, Integer> unlockedItemLimit) {
        this.unlockedItemLimit = unlockedItemLimit;
    }

    public UserContext unlockedItemLimit(Map<Integer, Integer> unlockedItemLimit) {
        setUnlockedItemLimit(unlockedItemLimit);
        return this;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public UserContext xp(int xp) {
        setXp(xp);
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
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", levelId=" + levelId +
                ", unlockedItemLimit=" + unlockedItemLimit +
                ", xp=" + xp +
                '}';
    }

    public enum RegisterState {
        UNREGISTERED,
        EMAIL_UNVERIFIED,
        EMAIL_VERIFIED,
        FACEBOOK
    }
}
