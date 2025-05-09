package com.btxtech.shared.gameengine.datatypes;

/**
 * Created by Beat
 * 25.07.2016.
 */
public enum Character {
    HUMAN(false),
    BOT(true),
    BOT_NCP(true);

    private final boolean bot;

    Character(boolean bot) {
        this.bot = bot;
    }

    public boolean isBot() {
        return bot;
    }

    public boolean isHuman() {
        return !bot;
    }

    public boolean isEnemy(Character other) {
        return !(this == Character.BOT && other == Character.BOT) && !(this != Character.BOT && other != Character.BOT);

    }
}
