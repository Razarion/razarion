package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.planet.model.SyncConsumer;
import com.btxtech.shared.gameengine.planet.model.SyncGenerator;

/**
 * Created by Beat
 * 16.07.2016.
 */
public class EnergyService {
    public void generatorActivated(SyncGenerator syncGenerator) {
        throw new UnsupportedOperationException();
    }

    public void generatorDeactivated(SyncGenerator syncGenerator) {
        throw new UnsupportedOperationException();
    }

    public void consumerActivated(SyncConsumer syncConsumer) {
        throw new UnsupportedOperationException();
    }

    public void consumerDeactivated(SyncConsumer syncConsumer) {
        throw new UnsupportedOperationException();
    }

    // TODO int getConsuming();

    // TODO int getGenerating();

    // TODO void onBaseKilled(PlayerBase simpleBase);

}
