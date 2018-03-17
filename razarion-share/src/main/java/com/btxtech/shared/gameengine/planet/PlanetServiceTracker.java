package com.btxtech.shared.gameengine.planet;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 29.09.2017.
 */
public class PlanetServiceTracker {
    public static final int TICKS_FOR_DUMP = 100;
    private Logger logger = Logger.getLogger(PlanetServiceTracker.class.getName());
    private long startPeriodTimeStamp;
    private long startTimeStamp;
    private long startTickTimeStamp;
    private int tickCount = TICKS_FOR_DUMP;
    private int totalTickTime;
    private int questServiceTime;
    private int pathingServiceTime;
    private int projectileServiceTime;
    private int energyServiceTime;
    private int baseItemServiceTime;
    private int boxServiceTime;
    private int afterTickListenerTime;
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
    }

    public void startTick() {
        if (!running) {
            return;
        }
        startTimeStamp = System.currentTimeMillis();
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
        pathingServiceTime += calculateDifAndReload();
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
        totalTickTime += (System.currentTimeMillis() - startTickTimeStamp);
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
                "-------------------------------------------------");
    }
}
