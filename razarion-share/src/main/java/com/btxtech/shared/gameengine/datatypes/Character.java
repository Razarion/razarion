package com.btxtech.shared.gameengine.datatypes;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 25.07.2016.
 */
@Portable
public enum Character {
    HUMAN(false),
    BOT(true),
    BOT_NCP(true),;

    private boolean bot;

    Character(boolean bot) {
        this.bot = bot;
    }

    public boolean isBot() {
        return bot;
    }

    public boolean isHuman() {
        return !bot;
    }
}
