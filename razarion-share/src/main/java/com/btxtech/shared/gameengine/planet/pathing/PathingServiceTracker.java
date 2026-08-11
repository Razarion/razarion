package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.gameengine.planet.PlanetServiceTracker;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 29.09.2017.
 */
public class PathingServiceTracker {
    private final Logger logger = Logger.getLogger(PathingServiceTracker.class.getName());
    private long startPeriodTimeStamp;
    private long startTimeStamp;
    private long startTickTimeStamp;
    private int tickCount = PlanetServiceTracker.TICKS_FOR_DUMP;
    private int totalTickTime;
    private int preparationTime;
    private int solveVelocityTime;
    private int implementPositionTime;
    private int checkDestinationTime;
    private int finalizationTime;
    private int syncItemContainerServiceTime;
    private boolean running;

    /**
     * Switching on also starts a fresh period, so the first dump is a real 100-tick sample rather
     * than one measured from whenever the tracker happened to be enabled.
     */
    public void setRunning(boolean running) {
        this.running = running;
        clear();
    }

    public void clear() {
        if (!running) {
            return;
        }
        startPeriodTimeStamp = System.currentTimeMillis();
        tickCount = 0;
        totalTickTime = 0;
        preparationTime = 0;
        solveVelocityTime = 0;
        implementPositionTime = 0;
        checkDestinationTime = 0;
        finalizationTime = 0;
        syncItemContainerServiceTime = 0;
    }

    public void startTick() {
        if (!running) {
            return;
        }
        startTimeStamp = System.currentTimeMillis();
        startTickTimeStamp = startTimeStamp;
        tickCount++;
    }

    public void afterPreparation() {
        if (!running) {
            return;
        }
        preparationTime += calculateDifAndReload();
    }


    public void afterSolveVelocity() {
        if (!running) {
            return;
        }
        solveVelocityTime += calculateDifAndReload();
    }

    public void afterImplementPosition() {
        if (!running) {
            return;
        }
        implementPositionTime += calculateDifAndReload();
    }

    public void afterCheckDestination() {
        if (!running) {
            return;
        }
        checkDestinationTime += calculateDifAndReload();
    }

    public void afterFinalization() {
        if (!running) {
            return;
        }
        finalizationTime += calculateDifAndReload();
    }

    public void afterSyncItemContainerService() {
        if (!running) {
            return;
        }
        syncItemContainerServiceTime += calculateDifAndReload();
    }

    public void endTick() {
        if (!running) {
            return;
        }
        totalTickTime += (System.currentTimeMillis() - startTickTimeStamp);
        if (tickCount >= PlanetServiceTracker.TICKS_FOR_DUMP) {
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

    /**
     * The phases listed here are exactly the ones {@link PathingService#tick} measures, and together
     * they account for the whole tick. Phases the tick does not call have no line: a diagnostic that
     * prints "0.0s" for something it never timed reads as "this is free".
     */
    private void dump() {
        double periodTime = (System.currentTimeMillis() - startPeriodTimeStamp) / 1000.0;
        double ticksPerSecond = tickCount / periodTime;
        double factor = 1.0 / tickCount / 1000;
        logger.warning("\nPathingServiceTracker----------------------------\n" +
                "start time: " + new Date(startPeriodTimeStamp) + "s\n" +
                "periodTime: " + periodTime + "s\n" +
                "tickCount: " + tickCount + "\n" +
                "ticksPerSecond: " + ticksPerSecond + "\n" +
                "totalTickTime: " + totalTickTime * factor + "s\n" +
                "preparationTime: " + preparationTime * factor + "s\n" +
                "solveVelocityTime: " + solveVelocityTime * factor + "s\n" +
                "implementPositionTime: " + implementPositionTime * factor + "s\n" +
                "checkDestinationTime: " + checkDestinationTime * factor + "s\n" +
                "syncItemContainerServiceTime: " + syncItemContainerServiceTime * factor + "s\n" +
                "finalizationTime: " + finalizationTime * factor + "s\n" +
                "-------------------------------------------------");
    }
}
