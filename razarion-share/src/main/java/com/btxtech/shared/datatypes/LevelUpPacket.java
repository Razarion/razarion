package com.btxtech.shared.datatypes;

import org.dominokit.jackson.annotation.JSONMapper;

/**
 * Created by Beat
 * on 22.09.2017.
 */
@JSONMapper
public class LevelUpPacket {
    private UserContext userContext;
    private boolean availableUnlocks;

    public UserContext getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public boolean isAvailableUnlocks() {
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
