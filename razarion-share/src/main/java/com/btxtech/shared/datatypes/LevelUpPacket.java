package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * on 22.09.2017.
 */
public class LevelUpPacket {
    private UserContext userContext;
    private boolean availableUnlocks;

    public UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public boolean getAvailableUnlocks() {
        return availableUnlocks;
    }

    public void setAvailableUnlocks(boolean availableUnlocks) {
        this.availableUnlocks = availableUnlocks;
    }

    public LevelUpPacket userContext(UserContext userContext) {
        setUserContext(userContext);
        return this;
    }

    public LevelUpPacket availableUnlocks(boolean availableUnlocks) {
        setAvailableUnlocks(availableUnlocks);
        return this;
    }
}
