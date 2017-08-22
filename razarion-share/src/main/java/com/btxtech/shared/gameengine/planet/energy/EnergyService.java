package com.btxtech.shared.gameengine.planet.energy;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncConsumer;
import com.btxtech.shared.gameengine.planet.model.SyncGenerator;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

/**
 * Created by Beat
 * 16.07.2016.
 */
@Singleton
public class EnergyService {
    @Inject
    private Instance<BaseEnergy> baseEnergyInstance;
    private final HashMap<PlayerBase, BaseEnergy> baseEnergies = new HashMap<>();

    public void generatorActivated(SyncGenerator syncGenerator) {
        getBaseEnergy(syncGenerator.getSyncBaseItem()).generatorActivated(syncGenerator);
    }

    public void generatorDeactivated(SyncGenerator syncGenerator) {
        getBaseEnergy(syncGenerator.getSyncBaseItem()).generatorDeactivated(syncGenerator);
    }

    public void consumerActivated(SyncConsumer syncConsumer) {
        getBaseEnergy(syncConsumer.getSyncBaseItem()).consumerActivated(syncConsumer);
    }

    public void consumerDeactivated(SyncConsumer syncConsumer) {
        getBaseEnergy(syncConsumer.getSyncBaseItem()).consumerDeactivated(syncConsumer);
    }

    public void onBaseItemKilled(SyncBaseItem syncBaseItem) {
        if (syncBaseItem.getSyncConsumer() != null) {
            consumerDeactivated(syncBaseItem.getSyncConsumer());
        }

        if (syncBaseItem.getSyncGenerator() != null) {
            generatorDeactivated(syncBaseItem.getSyncGenerator());
        }
    }


    public void onBaseKilled(PlayerBase playerBase) {
        synchronized (baseEnergies) {
            baseEnergies.remove(playerBase);
        }
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
