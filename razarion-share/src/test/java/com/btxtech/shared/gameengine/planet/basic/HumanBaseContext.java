package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * Created by Beat
 * on 01.12.2017.
 */
public class HumanBaseContext {
    private UserContext userContext;
    private SyncBaseItem builder;
    private SyncBaseItem factory;
    private SyncBaseItem attacker;
    private PlayerBaseFull playerBaseFull;

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public void setBuilder(SyncBaseItem builder) {
        this.builder = builder;
    }

    public SyncBaseItem getBuilder() {
        return builder;
    }

    public void setFactory(SyncBaseItem factory) {
        this.factory = factory;
    }

    public SyncBaseItem getFactory() {
        return factory;
    }

    public void setAttacker(SyncBaseItem attacker) {
        this.attacker = attacker;
    }

    public SyncBaseItem getAttacker() {
        return attacker;
    }

    public void setPlayerBaseFull(PlayerBaseFull playerBaseFull) {
        this.playerBaseFull = playerBaseFull;
    }

    public PlayerBaseFull getPlayerBaseFull() {
        return playerBaseFull;
    }
}
