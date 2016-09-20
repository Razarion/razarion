package com.btxtech.shared.system;

/**
 * Created by Beat
 * 27.06.2016.
 */
public interface SimpleExecutorService {
    enum Type {
        GAME_ENGINE,
        BOT,
        UNSPECIFIED
    }

    SimpleScheduledFuture schedule(long delayMilliS, Runnable runnable, Type type);

    SimpleScheduledFuture scheduleAtFixedRate(long delayMilliS, boolean start, Runnable runnable, Type type);
}
