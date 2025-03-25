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
    private int findContactsTime;
    private int solveVelocityTime;
    private int implementPositionTime;
    private int solvePositionTime;
    private int checkDestinationTime;
    private int finalizationTime;
    private int updateListenerTime;
    private int syncItemContainerServiceTime;
    private final boolean running;

    public PathingServiceTracker(boolean running) {
        this.running = running;
    }

    public void clear() {
        if (!running) {
            return;
        }
        startPeriodTimeStamp = System.currentTimeMillis();
        tickCount = 0;
        totalTickTime = 0;
        preparationTime = 0;
        findContactsTime = 0;
        solveVelocityTime = 0;
        implementPositionTime = 0;
        solvePositionTime = 0;
        checkDestinationTime = 0;
        finalizationTime = 0;
        updateListenerTime = 0;
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


    public void afterFindContacts() {
        if (!running) {
            return;
        }
        findContactsTime += calculateDifAndReload();
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

    public void afterSolvePosition() {
        if (!running) {
            return;
        }
        solvePositionTime += calculateDifAndReload();
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

    public void afterUpdateListener() {
        if (!running) {
            return;
        }
        updateListenerTime += calculateDifAndReload();
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
                "findContactsTime: " + findContactsTime * factor + "s\n" +
                "solveVelocityTime: " + solveVelocityTime * factor + "s\n" +
                "implementPositionTime: " + implementPositionTime * factor + "s\n" +
                "solvePositionTime: " + solvePositionTime * factor + "s\n" +
                "checkDestinationTime: " + checkDestinationTime * factor + "s\n" +
                "finalizationTime: " + finalizationTime * factor + "s\n" +
                "updateListenerTime: " + updateListenerTime * factor + "s\n" +
                "syncItemContainerServiceTime: " + syncItemContainerServiceTime * factor + "s\n" +
                "-------------------------------------------------");
    }
}
