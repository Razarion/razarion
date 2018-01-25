package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * on 24.01.2018.
 */
public class RegisterInfo {
    private HumanPlayerId humanPlayerId;
    private boolean userAlreadyExits;

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public RegisterInfo setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
        return this;
    }

    public boolean isUserAlreadyExits() {
        return userAlreadyExits;
    }

    public RegisterInfo setUserAlreadyExits(boolean userAlreadyExits) {
        this.userAlreadyExits = userAlreadyExits;
        return this;
    }
}
