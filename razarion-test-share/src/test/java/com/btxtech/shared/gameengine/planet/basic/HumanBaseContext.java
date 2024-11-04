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
    private SyncBaseItem attacker1;
    private SyncBaseItem attacker2;
    private SyncBaseItem attacker3;
    private SyncBaseItem attacker4;
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

    public void setAttacker1(SyncBaseItem attacker1) {
        this.attacker1 = attacker1;
    }

    public SyncBaseItem getAttacker1() {
        return attacker1;
    }

    public SyncBaseItem getAttacker2() {
        return attacker2;
    }

    public void setAttacker2(SyncBaseItem attacker2) {
        this.attacker2 = attacker2;
    }

    public SyncBaseItem getAttacker3() {
        return attacker3;
    }

    public void setAttacker3(SyncBaseItem attacker3) {
        this.attacker3 = attacker3;
    }

    public SyncBaseItem getAttacker4() {
        return attacker4;
    }

    public void setAttacker4(SyncBaseItem attacker4) {
        this.attacker4 = attacker4;
    }

    public void setPlayerBaseFull(PlayerBaseFull playerBaseFull) {
        this.playerBaseFull = playerBaseFull;
    }

    public PlayerBaseFull getPlayerBaseFull() {
        return playerBaseFull;
    }
}
