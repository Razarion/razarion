package com.btxtech.shared.gameengine.planet.energy;

import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncConsumer;
import com.btxtech.shared.gameengine.planet.model.SyncGenerator;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;

/**
 * Created by Beat
 * 16.07.2016.
 */
@Singleton
public class EnergyService {

    private final Provider<BaseEnergy> baseEnergyInstance;

    private final ExceptionHandler exceptionHandler;
    private final MapCollection<PlayerBase, SyncConsumer> changedSyncConsumers = new MapCollection<>();
    private final MapCollection<PlayerBase, SyncGenerator> changedSyncGenerators = new MapCollection<>();
    private final Set<PlayerBase> removedBases = new HashSet<>();
    private final HashMap<PlayerBase, BaseEnergy> baseEnergies = new HashMap<>();

    @Inject
    public EnergyService(ExceptionHandler exceptionHandler, Provider<com.btxtech.shared.gameengine.planet.energy.BaseEnergy> baseEnergyInstance) {
        this.exceptionHandler = exceptionHandler;
        this.baseEnergyInstance = baseEnergyInstance;
    }

    public void consumerActivated(SyncConsumer syncConsumer) {
        synchronized (changedSyncConsumers) {
            changedSyncConsumers.put(syncConsumer.getSyncBaseItem().getBase(), syncConsumer);
        }
    }

    public void generatorActivated(SyncGenerator syncGenerator) {
        synchronized (changedSyncGenerators) {
            changedSyncGenerators.put(syncGenerator.getSyncBaseItem().getBase(), syncGenerator);
        }
    }

    public void onBaseItemRemoved(SyncBaseItem syncBaseItem) {
        if (syncBaseItem.getSyncConsumer() != null) {
            synchronized (changedSyncConsumers) {
                changedSyncConsumers.put(syncBaseItem.getBase(), syncBaseItem.getSyncConsumer());
            }
        }
        if (syncBaseItem.getSyncGenerator() != null) {
            synchronized (changedSyncGenerators) {
                changedSyncGenerators.put(syncBaseItem.getBase(), syncBaseItem.getSyncGenerator());
            }
        }
    }

    public void onBaseKilled(PlayerBase playerBase) {
        synchronized (removedBases) {
            removedBases.add(playerBase);
        }
    }

    public void tick() {
        try {
            Collection<PlayerBase> changedBases = new ArrayList<>();
            synchronized (changedSyncConsumers) {
                changedSyncConsumers.iterate((playerBase, syncConsumer) -> {
                    if (!removedBases.contains(playerBase)) {
                        changedBases.add(playerBase);
                        if (syncConsumer.getSyncBaseItem().isAlive()) {
                            getBaseEnergy(syncConsumer.getSyncBaseItem()).consumerActivated(syncConsumer);
                        } else {
                            getBaseEnergy(syncConsumer.getSyncBaseItem()).consumerDeactivated(syncConsumer);
                        }
                    }
                    return true;
                });
                changedSyncConsumers.clear();
            }
            synchronized (changedSyncGenerators) {
                changedSyncGenerators.iterate((playerBase, syncGenerator) -> {
                    if (!removedBases.contains(playerBase)) {
                        changedBases.add(playerBase);
                        if (syncGenerator.getSyncBaseItem().isAlive()) {
                            getBaseEnergy(syncGenerator.getSyncBaseItem()).generatorActivated(syncGenerator);
                        } else {
                            getBaseEnergy(syncGenerator.getSyncBaseItem()).generatorDeactivated(syncGenerator);
                        }
                    }
                    return true;
                });
                changedSyncGenerators.clear();
            }
            synchronized (removedBases) {
                synchronized (baseEnergies) {
                    removedBases.forEach(baseEnergies::remove);
                }
                removedBases.clear();
            }
            changedBases.forEach(playerBase -> getBaseEnergy(playerBase).recalculate());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void clean() {
        changedSyncConsumers.clear();
        changedSyncGenerators.clear();
        removedBases.clear();
        baseEnergies.clear();
    }

    private BaseEnergy getBaseEnergy(SyncBaseItem syncBaseItem) {
        PlayerBase playerBase = syncBaseItem.getBase();
        return getBaseEnergy(playerBase);
    }

    private BaseEnergy getBaseEnergy(PlayerBase playerBase) {
        synchronized (baseEnergies) {
            BaseEnergy baseEnergy = baseEnergies.get(playerBase);
            if (baseEnergy == null) {
                baseEnergy = baseEnergyInstance.get();
                baseEnergy.setBase(playerBase);
                baseEnergies.put(playerBase, baseEnergy);
            }
            return baseEnergy;
        }
    }

}
