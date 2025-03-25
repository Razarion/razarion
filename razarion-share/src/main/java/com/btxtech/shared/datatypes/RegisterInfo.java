package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * on 24.01.2018.
 */
@Deprecated
public class RegisterInfo {
    private boolean humanPlayerId;
    private boolean userAlreadyExits;

    public boolean isRegistered() {
        return humanPlayerId;
    }

    public boolean isUserAlreadyExits() {
        return userAlreadyExits;
    }

    public RegisterInfo setUserAlreadyExits(boolean userAlreadyExits) {
        this.userAlreadyExits = userAlreadyExits;
        return this;
    }
}
