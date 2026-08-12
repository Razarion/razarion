package com.btxtech.shared.gameengine.planet;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 29.09.2017.
 */
public class PlanetServiceTracker {
    public static final int TICKS_FOR_DUMP = 100;
    /** A tick that takes longer than the interval it is scheduled at cannot keep the game at speed. */
    private static final int TICK_BUDGET_MILLIS = PlanetService.TICK_TIME_MILLI_SECONDS;
    /** System.currentTimeMillis() is never 0, so it can mark "no tick seen yet in this period". */
    private static final long NO_PREVIOUS_TICK = 0;
    private final Logger logger = Logger.getLogger(PlanetServiceTracker.class.getName());
    private long startPeriodTimeStamp;
    private long startTimeStamp;
    private long startTickTimeStamp;
    private long previousStartTickTimeStamp = NO_PREVIOUS_TICK;
    private int tickCount = TICKS_FOR_DUMP;
    private int totalTickTime;
    private int questServiceTime;
    private int pathingServiceTime;
    private int projectileServiceTime;
    private int energyServiceTime;
    private int baseItemServiceTime;
    private int boxServiceTime;
    private int afterTickListenerTime;
    private int maxTotalTickTime;
    private int maxPathingServiceTime;
    private int ticksOverBudget;
    /**
     * Distance between the starts of two consecutive ticks. The averages above say how long a tick
     * worked, not when it ran: a worker that is starved, throttled in a background tab or blocked by
     * something outside the tick shows up here and nowhere else.
     */
    private int totalTickGap;
    private int maxTickGap;
    private int tickGapCount;
    private boolean running;

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void clear() {
        if (!running) {
            return;
        }
        startPeriodTimeStamp = System.currentTimeMillis();
        tickCount = 0;
        totalTickTime = 0;
        questServiceTime = 0;
        pathingServiceTime = 0;
        projectileServiceTime = 0;
        energyServiceTime = 0;
        baseItemServiceTime = 0;
        boxServiceTime = 0;
        afterTickListenerTime = 0;
        maxTotalTickTime = 0;
        maxPathingServiceTime = 0;
        ticksOverBudget = 0;
        totalTickGap = 0;
        maxTickGap = 0;
        tickGapCount = 0;
        previousStartTickTimeStamp = NO_PREVIOUS_TICK;
    }

    public void startTick() {
        if (!running) {
            return;
        }
        startTimeStamp = System.currentTimeMillis();
        // The first tick of a period has no predecessor - measuring against the period start would
        // report the dump itself as a gap.
        if (previousStartTickTimeStamp != NO_PREVIOUS_TICK) {
            int gap = (int) (startTimeStamp - previousStartTickTimeStamp);
            totalTickGap += gap;
            tickGapCount++;
            maxTickGap = Math.max(maxTickGap, gap);
        }
        previousStartTickTimeStamp = startTimeStamp;
        startTickTimeStamp = startTimeStamp;
        tickCount++;
    }

    public void afterQuestService() {
        if (!running) {
            return;
        }
        questServiceTime += calculateDifAndReload();
    }

    public void afterPathingService() {
        if (!running) {
            return;
        }
        int delta = calculateDifAndReload();
        pathingServiceTime += delta;
        maxPathingServiceTime = Math.max(maxPathingServiceTime, delta);
    }

    public void afterProjectileService() {
        if (!running) {
            return;
        }
        projectileServiceTime += calculateDifAndReload();
    }

    public void afterEnergyService() {
        if (!running) {
            return;
        }
        energyServiceTime += calculateDifAndReload();
    }

    public void afterBaseItemService() {
        if (!running) {
            return;
        }
        baseItemServiceTime += calculateDifAndReload();
    }

    public void afterBoxService() {
        if (!running) {
            return;
        }
        boxServiceTime += calculateDifAndReload();
    }


    public void afterTickListener() {
        if (!running) {
            return;
        }
        afterTickListenerTime += calculateDifAndReload();
    }

    public void endTick() {
        if (!running) {
            return;
        }
        int tickTime = (int) (System.currentTimeMillis() - startTickTimeStamp);
        totalTickTime += tickTime;
        maxTotalTickTime = Math.max(maxTotalTickTime, tickTime);
        if (tickTime > TICK_BUDGET_MILLIS) {
            ticksOverBudget++;
        }
        if (tickCount >= TICKS_FOR_DUMP) {
            dump();
            clear();
        }
    }

    private int calculateDifAndReload() {
        long currentTimeStamp = System.currentTimeMillis();
        int delta = (int) (currentTimeStamp - startTimeStamp);
        startTimeStamp = currentTimeStamp;
        return delta;
    }

    private void dump() {
        double periodTime = (System.currentTimeMillis() - startPeriodTimeStamp) / 1000.0;
        double ticksPerSecond = tickCount / periodTime;
        double factor = 1.0 / tickCount / 1000;
        logger.warning("\nPlanetServiceTracker----------------------------\n" +
                "start time: " + new Date(startPeriodTimeStamp) + "s\n" +
                "periodTime: " + periodTime + "s\n" +
                "tickCount: " + tickCount + "\n" +
                "ticksPerSecond: " + ticksPerSecond + "\n" +
                "totalTickTime: " + totalTickTime * factor + "s\n" +
                "questServiceTime: " + questServiceTime * factor + "s\n" +
                "pathingServiceTime: " + pathingServiceTime * factor + "s\n" +
                "projectileServiceTime: " + projectileServiceTime * factor + "s\n" +
                "energyServiceTime: " + energyServiceTime * factor + "s\n" +
                "baseItemServiceTime: " + baseItemServiceTime * factor + "s\n" +
                "boxServiceTime: " + boxServiceTime * factor + "s\n" +
                "afterTickListenerTime: " + afterTickListenerTime * factor + "s\n" +
                "maxTotalTickTime: " + maxTotalTickTime / 1000.0 + "s\n" +
                "maxPathingServiceTime: " + maxPathingServiceTime / 1000.0 + "s\n" +
                "ticksOverBudget: " + ticksOverBudget + " (>" + TICK_BUDGET_MILLIS + "ms)\n" +
                "avgTickGap: " + (tickGapCount > 0 ? totalTickGap / (double) tickGapCount / 1000.0 : 0.0) + "s\n" +
                "maxTickGap: " + maxTickGap / 1000.0 + "s\n" +
                "-------------------------------------------------");
    }
}
