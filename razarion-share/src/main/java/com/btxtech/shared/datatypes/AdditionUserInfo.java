package com.btxtech.shared.datatypes;

import java.util.Date;

/**
 * Created by Beat
 * on 12.03.2018.
 */
public class AdditionUserInfo {
    private HumanPlayerId humanPlayerId;
    private Date lastLoggedIn;

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public void setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
    }

    public Date getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(Date lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }
}
