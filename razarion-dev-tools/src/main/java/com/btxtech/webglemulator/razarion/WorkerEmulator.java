package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.GameEngineControlPackage;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.01.2017.
 */
@ApplicationScoped
public class WorkerEmulator {
    @Inject
    private DevToolGameEngineControl gameEngineControl;
    private DevToolGameEngineWorker gameEngineWorker;
    private WeldContainer weldContainer;

    @PostConstruct
    public void postConstruct() {
        Weld weld = new Weld();
        weldContainer = weld.initialize();
        gameEngineWorker = weldContainer.instance().select(DevToolGameEngineWorker.class).get();
        gameEngineWorker.setPackageConsumer(gameEngineControl::receivePackage);
    }

    void sendPackage(GameEngineControlPackage gameEngineControlPackage) {
        gameEngineWorker.receivePackage(gameEngineControlPackage);
    }

    public WeldContainer getWeldContainer() {
        return weldContainer;
    }
}
