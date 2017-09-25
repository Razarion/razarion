package com.btxtech.shared.system.perfmon;

/**
 * User: beat
 * Date: 26.07.12
 * Time: 15:00
 */
public enum PerfmonEnum {
    RENDERER("Renderer", true),
    GAME_ENGINE("Game Engine", true),
    BOT("Bot", false),
    DETAILED_TRACKING("Detailed Tracking", false);

    private String displayName;
    private boolean fps;

    PerfmonEnum(String displayName, boolean fps) {
        this.displayName = displayName;
        this.fps = fps;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFps() {
        return fps;
    }
}
