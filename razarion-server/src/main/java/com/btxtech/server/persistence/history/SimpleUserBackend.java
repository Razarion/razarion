package com.btxtech.server.persistence.history;

import com.btxtech.shared.datatypes.HumanPlayerId;

/**
 * Created by Beat
 * on 11.01.2018.
 */
public class SimpleUserBackend {
    private HumanPlayerId humanPlayerId;
    private String name;

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public SimpleUserBackend setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
        return this;
    }

    public String getName() {
        return name;
    }

    public SimpleUserBackend setName(String name) {
        this.name = name;
        return this;
    }
}
