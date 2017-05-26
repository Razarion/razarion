package com.btxtech.shared.system;

import com.btxtech.shared.system.perfmon.PerfmonEnum;

/**
 * Created by Beat
 * 27.06.2016.
 */
public interface SimpleExecutorService {
    enum Type {
        GAME_ENGINE(PerfmonEnum.GAME_ENGINE),
        BOT(PerfmonEnum.BOT),
        DETAILED_TRACKING(PerfmonEnum.DETAILED_TRACKING),
        UNSPECIFIED(null);

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
