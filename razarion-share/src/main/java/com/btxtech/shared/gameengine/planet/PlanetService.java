package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class PlanetService implements Runnable {
    public static final PlanetMode MODE = PlanetMode.MASTER;
    public static final int TICK_TIME_MILLI_SECONDS = 100;
    public static final double TICK_FACTOR = (double) TICK_TIME_MILLI_SECONDS / 1000.0;
    // private Logger logger = Logger.getLogger(PlanetService.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Event<PlanetActivationEvent> activationEvent;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private PathingService pathingService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private QuestService questService;
    @Inject
    private BoxService boxService;
    @Inject
    private ProjectileService projectileService;
    private boolean pause;
    private SimpleScheduledFuture scheduledFuture;
    private PlanetConfig planetConfig;
    private long tickCount;
    private Collection<PlanetTickListener> tickListeners = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        scheduledFuture = simpleExecutorService.scheduleAtFixedRate(TICK_TIME_MILLI_SECONDS, false, this, SimpleExecutorService.Type.GAME_ENGINE);
    }

    public void initialise(PlanetConfig planetConfig) {
        tickCount = 0;
        this.planetConfig = planetConfig;
        activationEvent.fire(new PlanetActivationEvent(planetConfig));
    }

    public void start() {
        scheduledFuture.start();
    }

    @Override
    public void run() {
        if (pause) {
            return;
        }
        try {
            pathingService.tick();
            questService.checkPositionCondition();
            baseItemService.tick();
            boxService.tick();
            projectileService.tick();
            notifyTickListeners();
            tickCount++;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public long getTickCount() {
        return tickCount;
    }

    PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void stop() {
        scheduledFuture.cancel();
    }

    public void addTickListener(PlanetTickListener planetTickListener) {
        tickListeners.add(planetTickListener);
    }

    public void removeTickListener(PlanetTickListener planetTickListener) {
        tickListeners.remove(planetTickListener);
    }

    private void notifyTickListeners() {
        for (PlanetTickListener tickListener : tickListeners) {
            tickListener.onPostTick();
        }
    }

}
