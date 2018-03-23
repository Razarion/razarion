package com.btxtech.shared.system.perfmon;

/**
 * User: beat
 * Date: 26.07.12
 * Time: 15:00
 */
public enum PerfmonEnum {
    RENDERER(true),
    GAME_ENGINE(true),
    CLIENT_GAME_ENGINE_UPDATE(true),
    BOT_TICKER(true),
    BOT_SCENE_TICKER(true),
    BOT_TIMER(true),
    DETAILED_TRACKING(true),
    COVER_FADE(true),
    DRAW_MINI_MAP(true),
    PERFMON_SEND_TO_CLIENT(true),
    PERFMON_ANALYSE(true),
    PLAYBACK(true),
    SCENE_RUNNER(true),
    SCENE_WAIT(true),
    TRAIL_SERVICE(true),
    SCROLL(true),
    SCROLL_AUTO(true),
    TIP_SCROLL(true),
    TIP_SPAWN(true),
    TIP_GUI_POINTING(true),
    REGISTER(true),
    USER_SET_NAME(true),
    SERVER_RESTART_WATCHDOG(true),
    RELOAD_CLIENT_WRONG_INTERFACE_VERSION(true),
    ESTABLISH_CONNECTION(true),
    WAIT_RESTART(true),
    QUEST_PROGRESS_PANEL_TEXT_REFRESHER(true);

    private boolean fps;

    PerfmonEnum(boolean fps) {
        this.fps = fps;
    }

    public boolean isFps() {
        return fps;
    }
}
