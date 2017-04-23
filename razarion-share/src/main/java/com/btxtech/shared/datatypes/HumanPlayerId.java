package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 23.04.2017.
 */
public class HumanPlayerId {
    private int playerId;
    private Integer userId;

    public int getPlayerId() {
        return playerId;
    }

    public HumanPlayerId setPlayerId(int playerId) {
        this.playerId = playerId;
        return this;
    }

    public Integer getUserId() {
        return userId;
    }

    public HumanPlayerId setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public String toString() {
        return "HumanPlayerId{" +
                "playerId=" + playerId +
                ", userId=" + userId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HumanPlayerId other = (HumanPlayerId) o;
        return playerId == other.playerId;
    }

    @Override
    public int hashCode() {
        return playerId;
    }
}
