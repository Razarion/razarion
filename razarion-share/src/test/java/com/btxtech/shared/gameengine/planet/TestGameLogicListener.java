package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 22.08.2017.
 */
public class TestGameLogicListener implements GameLogicListener {
    private List<EnergyStateChangedEntry> energyStateChangedEntries = new ArrayList<>();

    public void clearAll() {
        energyStateChangedEntries.clear();
    }

    public List<EnergyStateChangedEntry> getEnergyStateChangedEntries() {
        return energyStateChangedEntries;
    }

    @Override
    public void onEnergyStateChanged(PlayerBase base, int consuming, int generating) {
        energyStateChangedEntries.add(new EnergyStateChangedEntry(base, consuming, generating));
    }

    public static class EnergyStateChangedEntry {
        private PlayerBase base;
        private int generating;
        private int consuming;

        public EnergyStateChangedEntry(PlayerBase base, int consuming, int generating) {
            this.base = base;
            this.generating = generating;
            this.consuming = consuming;
        }

        public PlayerBase getBase() {
            return base;
        }

        public int getGenerating() {
            return generating;
        }

        public int getConsuming() {
            return consuming;
        }
    }
}
