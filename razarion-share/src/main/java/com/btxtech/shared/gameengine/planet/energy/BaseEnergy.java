package com.btxtech.shared.gameengine.planet.energy;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.model.SyncConsumer;
import com.btxtech.shared.gameengine.planet.model.SyncGenerator;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Beat
 * on 21.08.2017.
 */
@Dependent
public class BaseEnergy {
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private GameLogicService gameLogicService;
    private int generating;
    private int consuming;
    private HashSet<SyncGenerator> syncGenerators = new HashSet<SyncGenerator>();
    private HashSet<SyncConsumer> syncConsumers = new HashSet<SyncConsumer>();
    private final Object syncObject = new Object();
    private PlayerBase base;

    public void setBase(PlayerBase base) {
        this.base = base;
    }

    public void generatorActivated(SyncGenerator syncGenerator) {
        syncGenerators.add(syncGenerator);
        recalculateGeneration();
    }

    public void generatorDeactivated(SyncGenerator syncGenerator) {
        syncGenerators.remove(syncGenerator);
        recalculateGeneration();
    }

    public void consumerActivated(SyncConsumer syncConsumer) {
        syncConsumers.add(syncConsumer);
        recalculateConsumption();
        syncConsumer.setOperationState(hasEnoughPower(generating, consuming));
    }

    public void consumerDeactivated(SyncConsumer syncConsumer) {
        syncConsumers.remove(syncConsumer);
        recalculateConsumption();
    }

    protected void recalculateGeneration() {
        synchronized (syncObject) {
            int tmpGenerating = syncGenerators.stream().mapToInt(SyncGenerator::getWattage).sum();
            if (tmpGenerating == generating) {
                return;
            }
            int oldGenerating = generating;
            generating = tmpGenerating;

            if (hasEnoughPower(oldGenerating, consuming) != hasEnoughPower(generating, consuming)) {
                setConsumerState(hasEnoughPower(generating, consuming));
            }
            gameLogicService.onEnergyStateChanged(base, consuming, generating);
        }
    }

    protected void recalculateConsumption() {
        synchronized (syncObject) {
            int tmpConsuming = syncConsumers.stream().mapToInt(SyncConsumer::getWattage).sum();
            if (tmpConsuming == consuming) {
                return;
            }
            int oldConsuming = consuming;
            consuming = tmpConsuming;

            if (hasEnoughPower(generating, oldConsuming) != hasEnoughPower(generating, consuming)) {
                setConsumerState(hasEnoughPower(generating, consuming));
            }
            gameLogicService.onEnergyStateChanged(base, consuming, generating);
        }
    }

    private void setConsumerState(boolean operationState) {
        synchronized (syncObject) {
            for (SyncConsumer syncConsumer : syncConsumers) {
                syncConsumer.setOperationState(operationState);
                if (operationState) {
                    baseItemService.addToActiveItemQueue(syncConsumer.getSyncBaseItem());
                }
            }
        }
    }

    private boolean hasEnoughPower(int generating, int consuming) {
        return generating >= consuming;
    }

    protected Collection<SyncGenerator> getSyncGenerators() {
        return syncGenerators;
    }

    protected Collection<SyncConsumer> getSyncConsumers() {
        return syncConsumers;
    }

    public int getGenerating() {
        return generating;
    }

    public int getConsuming() {
        return consuming;
    }

    protected void setGenerating(int generating) {
        this.generating = generating;
    }

    protected void setConsuming(int consuming) {
        this.consuming = consuming;
    }
}
