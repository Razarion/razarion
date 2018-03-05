package com.btxtech.shared.system;

import com.btxtech.shared.system.perfmon.PerfmonEnum;

/**
 * Created by Beat
 * 27.06.2016.
 */
public interface SimpleExecutorService {
    enum Type {
        GAME_ENGINE(PerfmonEnum.GAME_ENGINE),
        BOT_TICKER(PerfmonEnum.BOT_TICKER),
        BOT_TIMER(PerfmonEnum.BOT_TIMER),
        DETAILED_TRACKING(PerfmonEnum.DETAILED_TRACKING),
        COVER_FADE(PerfmonEnum.COVER_FADE),
        DRAW_MINI_MAP(PerfmonEnum.DRAW_MINI_MAP),
        PERFMON_SEND_TO_CLIENT(PerfmonEnum.PERFMON_SEND_TO_CLIENT),
        PERFMON_ANALYSE(PerfmonEnum.PERFMON_ANALYSE),
        PLAYBACK(PerfmonEnum.PLAYBACK),
        SCENE_RUNNER(PerfmonEnum.SCENE_RUNNER),
        SCENE_WAIT(PerfmonEnum.SCENE_WAIT),
        TRAIL_SERVICE(PerfmonEnum.TRAIL_SERVICE),
        SCROLL(PerfmonEnum.SCROLL),
        SCROLL_AUTO(PerfmonEnum.SCROLL_AUTO),
        TIP_SCROLL(PerfmonEnum.TIP_SCROLL),
        TIP_SPAWN(PerfmonEnum.TIP_SPAWN),
        TIP_GUI_POINTING(PerfmonEnum.TIP_GUI_POINTING),
        REGISTER(PerfmonEnum.REGISTER),
        USER_SET_NAME(PerfmonEnum.USER_SET_NAME),
        SERVER_RESTART_WATCHDOG(PerfmonEnum.SERVER_RESTART_WATCHDOG),
        RELOAD_CLIENT_WRONG_INTERFACE_VERSION(PerfmonEnum.RELOAD_CLIENT_WRONG_INTERFACE_VERSION),
        ESTABLISH_CONNECTION(PerfmonEnum.ESTABLISH_CONNECTION),
        WAIT_RESTART(PerfmonEnum.WAIT_RESTART);

        private PerfmonEnum perfmonEnum;

        Type(PerfmonEnum perfmonEnum) {
            this.perfmonEnum = perfmonEnum;
        }

        public PerfmonEnum getPerfmonEnum() {
            return perfmonEnum;
        }
    }

    SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type);

    SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type);
}
