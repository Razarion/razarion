package com.btxtech.shared.system.perfmon;

/**
 * User: beat
 * Date: 26.07.12
 * Time: 15:00
 */
public enum PerfmonEnum {
    RENDERER("Renderer"),
    GAME_ENGINE("Game Engine"),
    BOT("Bot"),
    DETAILED_TRACKING("Detailed Tracking");

    private String displayName;

    PerfmonEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
