package com.btxtech.shared.gameengine.planet.testframework;

import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 26.10.2018.
 */
public class ScenarioTicks {
    private List<List<SyncBaseItemInfo>> actualMasterTicks = new ArrayList<>();
    private List<List<SyncBaseItemInfo>> actualSlaveTicks = new ArrayList<>();

    public int size() {
        return actualMasterTicks.size();
    }

    public List<SyncBaseItemInfo> getMasterTick(int index) {
        return actualMasterTicks.get(index);
    }

    public List<SyncBaseItemInfo> getSlaveTick(int index) {
        return actualSlaveTicks.get(index);
    }

    public void addMasterTick(List<SyncBaseItemInfo> syncBaseItemInfos) {
        actualMasterTicks.add(syncBaseItemInfos);
    }

    public void addSlaveTick(List<SyncBaseItemInfo> syncBaseItemInfos) {
        actualSlaveTicks.add(syncBaseItemInfos);
    }

    public void setActualMasterTicks(List<List<SyncBaseItemInfo>> actualMasterTicks) {
        this.actualMasterTicks = actualMasterTicks;
    }

    public void setActualSlaveTicks(List<List<SyncBaseItemInfo>> actualSlaveTicks) {
        this.actualSlaveTicks = actualSlaveTicks;
    }

    public List<List<SyncBaseItemInfo>> getActualMasterTicks() {
        return actualMasterTicks;
    }

    public List<List<SyncBaseItemInfo>> getActualSlaveTicks() {
        return actualSlaveTicks;
    }

    public void compareMasterSlave() {
        if (actualMasterTicks.size() != actualSlaveTicks.size()) {
            System.out.println("Master/Slave not same size. Master: " + actualMasterTicks.size() + ". Slave: " + actualSlaveTicks.size());
            return;
        }
        List<SyncBaseItemInfo> masterInfos = actualMasterTicks.get(actualMasterTicks.size() - 1);
        List<SyncBaseItemInfo> salveInfos = actualSlaveTicks.get(actualSlaveTicks.size() - 1);

        masterInfos.forEach(masterInfo -> {
            SyncBaseItemInfo slaveInfo = findSlaveInfo(masterInfo, salveInfos);
            double distance = masterInfo.getSyncPhysicalAreaInfo().getPosition().getDistance(slaveInfo.getSyncPhysicalAreaInfo().getPosition());
            if (distance > 0.00000001) {
                System.out.println("### id: " + masterInfo.getId() + ". d: " + distance + ". Master: " + masterInfo.getSyncPhysicalAreaInfo().getPosition() + ". Master: " + slaveInfo.getSyncPhysicalAreaInfo().getPosition());
            }
        });
    }

    private SyncBaseItemInfo findSlaveInfo(SyncBaseItemInfo masterInfo, List<SyncBaseItemInfo> salveInfos) {
        return salveInfos.stream().filter(slaveInfo -> slaveInfo.getId() == masterInfo.getId()).findFirst().orElseThrow(() -> new IllegalArgumentException("No such id id in slave found: " + masterInfo.getId()));
    }
}
