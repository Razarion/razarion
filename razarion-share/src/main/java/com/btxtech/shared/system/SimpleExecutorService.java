package com.btxtech.shared.system;

/**
 * Created by Beat
 * 27.06.2016.
 */
public interface SimpleExecutorService {
    enum Type {
        GAME_ENGINE,
        BOT_TICKER,
        BOT_SCENE_TICKER,
        BOT_TIMER,
        DETAILED_TRACKING,
        COVER_FADE,
        DRAW_MINI_MAP,
        SCENE_RUNNER,
        SCENE_WAIT,
        SCROLL,
        SCROLL_AUTO,
        TIP_SCROLL,
        TIP_SPAWN,
        TIP_GUI_POINTING,
        REGISTER,
        USER_SET_NAME,
        SERVER_RESTART_WATCHDOG,
        RELOAD_CLIENT_WRONG_INTERFACE_VERSION,
        ESTABLISH_CONNECTION,
        WAIT_RESTART,
        QUEST_PROGRESS_PANEL_TEXT_REFRESHER,
        BOOT_TASK_TIMEOUT
    }

    SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type);

    SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type);
}
