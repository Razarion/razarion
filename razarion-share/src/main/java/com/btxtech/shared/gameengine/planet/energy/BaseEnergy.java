package com.btxtech.shared.gameengine.planet.energy;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.model.SyncConsumer;
import com.btxtech.shared.gameengine.planet.model.SyncGenerator;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Beat
 * on 21.08.2017.
 */

public class BaseEnergy {

    private GameLogicService gameLogicService;
    private int consuming;
    private int generating;
    private final Set<SyncGenerator> syncGenerators = new HashSet<>();
    private final Set<SyncConsumer> syncConsumers = new HashSet<>();
    private final Object syncObject = new Object();
    private PlayerBase base;

    @Inject
    public BaseEnergy(GameLogicService gameLogicService) {
        this.gameLogicService = gameLogicService;
    }

    public void setBase(PlayerBase base) {
        this.base = base;
    }

    public void generatorActivated(SyncGenerator syncGenerator) {
        syncGenerators.add(syncGenerator);
    }

    public void generatorDeactivated(SyncGenerator syncGenerator) {
        syncGenerators.remove(syncGenerator);
    }

    public void consumerActivated(SyncConsumer syncConsumer) {
        syncConsumers.add(syncConsumer);
    }

    public void consumerDeactivated(SyncConsumer syncConsumer) {
        syncConsumers.remove(syncConsumer);
    }

    public void recalculate() {
        int newConsuming;
        synchronized (syncObject) {
            newConsuming = syncConsumers.stream().mapToInt(SyncConsumer::getWattage).sum();
        }
        int newGenerating;
        synchronized (syncObject) {
            newGenerating = syncGenerators.stream().mapToInt(SyncGenerator::getWattage).sum();
        }

        if(newConsuming == consuming && newGenerating == generating) {
            return;
        }
        consuming = newConsuming;
        generating = newGenerating;
        gameLogicService.onEnergyStateChanged(base, consuming, generating);
    }

    public int getGenerating() {
        return generating;
    }

    public int getConsuming() {
        return consuming;
    }
}
